package org.jbd.backend.user.service;

import org.jbd.backend.common.exception.BusinessException;
import org.jbd.backend.common.exception.ErrorCode;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.UserProfile;
import org.jbd.backend.user.domain.enums.Gender;
import org.jbd.backend.user.dto.UserProfileCreateDto;
import org.jbd.backend.user.dto.UserProfileResponseDto;
import org.jbd.backend.user.dto.UserProfileUpdateDto;
import org.jbd.backend.user.repository.UserProfileRepository;
import org.jbd.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public UserProfileService(UserProfileRepository userProfileRepository,
                             UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    public UserProfileResponseDto createUserProfile(Long userId, UserProfileCreateDto createDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 이미 프로필이 존재하는지 확인
        if (userProfileRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "이미 프로필이 존재합니다.");
        }

        UserProfile userProfile = new UserProfile(user, createDto.getFirstName(), createDto.getLastName());
        userProfile.setAge(createDto.getAge());
        userProfile.setGender(createDto.getGender());
        userProfile.setLocation(createDto.getLocation());
        userProfile.setDesiredJob(createDto.getDesiredJob());
        userProfile.setPhoneNumber(createDto.getPhoneNumber());
        userProfile.setBio(createDto.getBio());

        UserProfile savedProfile = userProfileRepository.save(userProfile);
        return UserProfileResponseDto.from(savedProfile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND));

        return UserProfileResponseDto.from(userProfile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfileByProfileId(Long profileId) {
        UserProfile userProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND));

        return UserProfileResponseDto.from(userProfile);
    }

    public UserProfileResponseDto updateUserProfile(Long userId, UserProfileUpdateDto updateDto) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND));

        if (updateDto.getFirstName() != null || updateDto.getLastName() != null ||
            updateDto.getAge() != null || updateDto.getGender() != null) {
            userProfile.updateBasicInfo(
                    updateDto.getFirstName() != null ? updateDto.getFirstName() : userProfile.getFirstName(),
                    updateDto.getLastName() != null ? updateDto.getLastName() : userProfile.getLastName(),
                    updateDto.getAge() != null ? updateDto.getAge() : userProfile.getAge(),
                    updateDto.getGender() != null ? updateDto.getGender() : userProfile.getGender()
            );
        }

        if (updateDto.getPhoneNumber() != null || updateDto.getLocation() != null) {
            userProfile.updateContactInfo(
                    updateDto.getPhoneNumber() != null ? updateDto.getPhoneNumber() : userProfile.getPhoneNumber(),
                    updateDto.getLocation() != null ? updateDto.getLocation() : userProfile.getLocation()
            );
        }

        if (updateDto.getDesiredJob() != null) {
            userProfile.updateJobPreference(updateDto.getDesiredJob());
        }

        if (updateDto.getProfileImageUrl() != null) {
            userProfile.updateProfileImage(updateDto.getProfileImageUrl());
        }

        if (updateDto.getBio() != null) {
            userProfile.updateBio(updateDto.getBio());
        }

        UserProfile savedProfile = userProfileRepository.save(userProfile);
        return UserProfileResponseDto.from(savedProfile);
    }

    public void deleteUserProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_PROFILE_NOT_FOUND));

        userProfileRepository.delete(userProfile);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> getProfilesByGender(Gender gender) {
        return userProfileRepository.findByGender(gender)
                .stream()
                .map(UserProfileResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> getProfilesByLocation(String location) {
        return userProfileRepository.findByLocationContaining(location)
                .stream()
                .map(UserProfileResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> getProfilesByDesiredJob(String job) {
        return userProfileRepository.findByDesiredJobContaining(job)
                .stream()
                .map(UserProfileResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> getProfilesByAgeRange(Integer minAge, Integer maxAge) {
        return userProfileRepository.findByAgeBetween(minAge, maxAge)
                .stream()
                .map(UserProfileResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> getAllActiveProfiles() {
        return userProfileRepository.findAllActiveProfiles()
                .stream()
                .map(UserProfileResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Object[] getDemographicStatistics() {
        return userProfileRepository.findDemographicStatistics();
    }
}