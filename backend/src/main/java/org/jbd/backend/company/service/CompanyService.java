package org.jbd.backend.company.service;

import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.company.domain.Company;
import org.jbd.backend.company.dto.CompanyProfileDto;
import org.jbd.backend.company.dto.CompanyUpdateDto;
import org.jbd.backend.company.repository.CompanyRepository;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 기업 프로필 관리 서비스
 *
 * 기업 사용자의 회사 프로필 생성, 조회, 수정, 삭제 등의 비즈니스 로직을 처리합니다.
 * 기업 사용자만 접근 가능하며, 사업자번호 중복 검증, 기업 정보 유효성 검사 등을
 * 수행합니다. 기본적으로 읽기 전용 트랜잭션으로 동작하며, 쓰기 작업에는 별도 트랜잭션을 적용합니다.
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see Company
 * @see CompanyProfileDto
 * @see CompanyUpdateDto
 */
@Service
public class CompanyService {

    /** 기업 데이터 액세스 리포지토리 */
    private final CompanyRepository companyRepository;

    /** 사용자 데이터 액세스 리포지토리 */
    private final UserRepository userRepository;

    /**
     * CompanyService 생성자
     *
     * @param companyRepository 기업 리포지토리
     * @param userRepository 사용자 리포지토리
     */
    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    /**
     * 기업 프로필 정보를 조회합니다.
     * 기업 사용자의 회사 프로필 정보를 반환합니다.
     * 프로필이 없는 경우 기본 프로필을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return CompanyProfileDto 기업 프로필 정보
     * @throws BusinessException 사용자가 존재하지 않으면 USER_NOT_FOUND,
     *                          기업 사용자가 아니면 COMPANY_ACCESS_DENIED
     * @see CompanyProfileDto
     */
    @Transactional
    public CompanyProfileDto getCompanyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isCompanyUser()) {
            throw new BusinessException(ErrorCode.COMPANY_ACCESS_DENIED);
        }

        Company company = companyRepository.findByUserId(userId)
                .orElse(null);

        if (company == null) {
            // 기업 프로필이 없으면 기본값으로 생성
            company = createDefaultCompanyProfile(user);
        }

        return new CompanyProfileDto(company);
    }

    /**
     * 기업 프로필 정보를 수정합니다.
     * 기존 기업 프로필을 업데이트하거나, 없는 경우 새로 생성합니다.
     * 사업자번호 중복 검사를 수행하여 중복 등록을 방지합니다.
     *
     * @param userId 사용자 ID
     * @param updateDto 기업 정보 수정 데이터
     *                  - companyName: 회사명
     *                  - businessNumber: 사업자번호 (중복 검사 수행)
     *                  - industry: 산업군
     *                  - location: 위치 정보
     *                  - establishmentDate: 설립일
     *                  - employeeCount: 직원 수
     *                  - revenue: 매출
     *                  - description: 회사 설명
     *                  - websiteUrl: 웹사이트 URL
     *                  - companyEmail: 회사 이메일
     *                  - phoneNumber: 전화번호
     *                  - address: 주소
     *                  - logoUrl: 로고 이미지 URL
     * @return CompanyProfileDto 수정된 기업 프로필 정보
     * @throws BusinessException 사용자가 존재하지 않으면 USER_NOT_FOUND,
     *                          기업 사용자가 아니면 COMPANY_ACCESS_DENIED,
     *                          사업자번호 중복이면 DUPLICATE_BUSINESS_NUMBER
     * @see CompanyUpdateDto
     * @see CompanyProfileDto
     */
    @Transactional
    public CompanyProfileDto updateCompanyProfile(Long userId, CompanyUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isCompanyUser()) {
            throw new BusinessException(ErrorCode.COMPANY_ACCESS_DENIED);
        }

        Company company = companyRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultCompanyProfile(user));

        // 사업자번호 중복 확인 (본인 제외)
        if (updateDto.getBusinessNumber() != null && !updateDto.getBusinessNumber().isEmpty()) {
            boolean isDuplicate = companyRepository.existsByBusinessNumber(updateDto.getBusinessNumber()) &&
                    !updateDto.getBusinessNumber().equals(company.getBusinessNumber());
            if (isDuplicate) {
                throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
            }
        }

        updateCompanyFromDto(company, updateDto);
        Company savedCompany = companyRepository.save(company);

        return new CompanyProfileDto(savedCompany);
    }

    /**
     * 새로운 기업 프로필을 생성합니다.
     * 기업 사용자로 전환된 사용자가 최초로 회사 프로필을 등록할 때 사용합니다.
     * 이미 프로필이 존재하는 경우 예외를 발생시킵니다.
     *
     * @param userId 사용자 ID
     * @param updateDto 기업 정보 생성 데이터 (수정데이터와 동일한 구조)
     * @return CompanyProfileDto 생성된 기업 프로필 정보
     * @throws BusinessException 사용자가 존재하지 않으면 USER_NOT_FOUND,
     *                          기업 사용자가 아니면 COMPANY_ACCESS_DENIED,
     *                          이미 프로필이 존재하면 COMPANY_PROFILE_ALREADY_EXISTS,
     *                          사업자번호 중복이면 DUPLICATE_BUSINESS_NUMBER
     * @see CompanyUpdateDto
     * @see CompanyProfileDto
     */
    @Transactional
    public CompanyProfileDto createCompanyProfile(Long userId, CompanyUpdateDto updateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isCompanyUser()) {
            throw new BusinessException(ErrorCode.COMPANY_ACCESS_DENIED);
        }

        if (companyRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.COMPANY_PROFILE_ALREADY_EXISTS);
        }

        // 사업자번호 중복 확인
        if (updateDto.getBusinessNumber() != null && !updateDto.getBusinessNumber().isEmpty()) {
            if (companyRepository.existsByBusinessNumber(updateDto.getBusinessNumber())) {
                throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
            }
        }

        Company company = new Company(user, updateDto.getCompanyName());
        updateCompanyFromDto(company, updateDto);

        Company savedCompany = companyRepository.save(company);
        return new CompanyProfileDto(savedCompany);
    }

    /**
     * 사용자를 위한 기본 기업 프로필을 생성합니다.
     * 기업 프로필이 없는 사용자가 조회를 시도할 때 사용자 이름을 회사명으로 하는 기본 프로필을 생성합니다.
     *
     * @param user 기업 프로필을 생성할 사용자
     * @return Company 생성되고 저장된 기본 기업 프로필
     * @see Company
     */
    private Company createDefaultCompanyProfile(User user) {
        Company company = new Company(user, user.getName());
        return companyRepository.save(company);
    }

    /**
     * DTO의 데이터를 기업 엔티티에 업데이트합니다.
     * 모든 기업 정보 필드를 DTO에서 엔티티로 복사합니다.
     * null 값도 그대로 설정되므로 호출자가 null 체크를 수행해야 합니다.
     *
     * @param company 업데이트할 기업 엔티티
     * @param updateDto 업데이트 데이터가 담긴 DTO
     * @see Company
     * @see CompanyUpdateDto
     */
    private void updateCompanyFromDto(Company company, CompanyUpdateDto updateDto) {
        company.setCompanyName(updateDto.getCompanyName());
        company.setBusinessNumber(updateDto.getBusinessNumber());
        company.setIndustry(updateDto.getIndustry());
        company.setLocation(updateDto.getLocation());
        company.setEstablishmentDate(updateDto.getEstablishmentDate());
        company.setEmployeeCount(updateDto.getEmployeeCount());
        company.setRevenue(updateDto.getRevenue());
        company.setDescription(updateDto.getDescription());
        company.setWebsiteUrl(updateDto.getWebsiteUrl());
        company.setCompanyEmail(updateDto.getCompanyEmail());
        company.setPhoneNumber(updateDto.getPhoneNumber());
        company.setAddress(updateDto.getAddress());
        company.setLogoUrl(updateDto.getLogoUrl());
    }

    /**
     * 사용자가 기업 프로필을 보유하고 있는지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @return boolean 기업 프로필 존재 여부 (true: 존재, false: 미존재)
     */
    @Transactional(readOnly = true)
    public boolean hasCompanyProfile(Long userId) {
        return companyRepository.existsByUserId(userId);
    }

    /**
     * 기업 프로필을 삭제합니다.
     * 등록된 기업 프로필을 영구적으로 삭제합니다. 복구할 수 없습니다.
     *
     * @param userId 사용자 ID
     * @throws BusinessException 사용자가 존재하지 않으면 USER_NOT_FOUND,
     *                          기업 사용자가 아니면 COMPANY_ACCESS_DENIED,
     *                          기업 프로필이 없으면 COMPANY_NOT_FOUND
     */
    @Transactional
    public void deleteCompanyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isCompanyUser()) {
            throw new BusinessException(ErrorCode.COMPANY_ACCESS_DENIED);
        }

        Company company = companyRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMPANY_NOT_FOUND));

        companyRepository.delete(company);
    }
}