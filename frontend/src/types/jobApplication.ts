/**
 * Job Application 관련 타입 정의
 * Backend DTO와 매핑되는 타입들
 */

import { ApplicationStatus, JobPosting, User } from './api'

/**
 * 백엔드 JobApplicationResponseDto와 매핑
 * GET /api/applications/job/{id} 응답 타입
 */
export interface JobApplicationResponse {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  jobPostingId: number;
  jobPostingTitle: string;
  companyName: string;
  status: ApplicationStatus;
  coverLetter?: string;
  resumeUrl?: string;
  portfolioUrls?: string;
  appliedAt: string | number[];
  reviewedAt?: string | number[] | null;
  interviewerNotes?: string | null;
  rejectionReason?: string | null;
  interviewScheduledAt?: string | number[] | null;
  finalDecisionAt?: string | number[] | null;
}

/**
 * 프론트엔드에서 사용하는 확장된 JobApplication 타입
 * 필요시 User와 JobPosting 객체를 포함
 */
export interface JobApplicationWithDetails extends JobApplicationResponse {
  applicant?: User;
  jobPosting?: JobPosting;
}