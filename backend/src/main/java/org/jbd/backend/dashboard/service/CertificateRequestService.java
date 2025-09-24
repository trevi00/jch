package org.jbd.backend.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.dashboard.domain.CertificateRequest;
import org.jbd.backend.dashboard.dto.CertificateRequestDto;
import org.jbd.backend.dashboard.repository.CertificateRequestRepository;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateRequestService {

    private final CertificateRequestRepository certificateRequestRepository;
    private final UserService userService;

    @Transactional
    public CertificateRequestDto.ResponseDto requestCertificate(Long userId, CertificateRequestDto.CreateDto dto) {
        User user = userService.findUserById(userId);
        
        CertificateRequest request = new CertificateRequest(user, dto.getCertificateType(), dto.getPurpose());
        CertificateRequest saved = certificateRequestRepository.save(request);
        
        return CertificateRequestDto.ResponseDto.from(saved);
    }

    public List<CertificateRequestDto.ResponseDto> getUserRequests(Long userId) {
        User user = userService.findUserById(userId);
        List<CertificateRequest> requests = certificateRequestRepository.findByUserOrderByCreatedAtDesc(user);
        
        return requests.stream()
                .map(CertificateRequestDto.ResponseDto::from)
                .collect(Collectors.toList());
    }

    public Page<CertificateRequestDto.ResponseDto> getAllRequests(Pageable pageable) {
        Page<CertificateRequest> requests = certificateRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        return requests.map(CertificateRequestDto.ResponseDto::from);
    }

    @Transactional
    public CertificateRequestDto.ResponseDto processRequest(Long requestId, CertificateRequestDto.ProcessDto dto) {
        CertificateRequest request = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "증명서 요청을 찾을 수 없습니다"));
        
        if (dto.getApproved()) {
            request.approve(dto.getAdminNotes());
        } else {
            request.reject(dto.getAdminNotes());
        }
        
        CertificateRequest saved = certificateRequestRepository.save(request);
        return CertificateRequestDto.ResponseDto.from(saved);
    }

    @Transactional
    public CertificateRequestDto.ResponseDto completeRequest(Long requestId) {
        CertificateRequest request = certificateRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "증명서 요청을 찾을 수 없습니다"));
        
        request.complete();
        CertificateRequest saved = certificateRequestRepository.save(request);
        
        return CertificateRequestDto.ResponseDto.from(saved);
    }
}