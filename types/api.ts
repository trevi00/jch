// ============= Common Types =============
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  errorCode?: string;
  timestamp?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// ============= User Types =============
export interface User {
  id: number;
  email: string;
  name: string;
  userType: UserType;
  employmentStatus?: EmploymentStatus;
  profileImage?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserProfile {
  id: number;
  userId: number;
  name?: string;
  email?: string;
  phoneNumber?: string;
  website?: string;
  summary?: string;
  age?: number;
  gender?: Gender;
  location?: string;
  desiredPosition?: string;
  desiredCompany?: string;
  educations: Education[];
  skills: Skill[];
  certifications: Certification[];
  experiences: Experience[];
  portfolios: Portfolio[];
}

export interface Education {
  id: number;
  userId: number;
  degree: EducationLevel;
  school: string;
  major: string;
  graduationYear: number;
  gpa?: number;
  maxGpa?: number;
}

export interface Skill {
  id: number;
  userId: number;
  name: string;
  category: SkillCategory;
  level: SkillLevel;
}

export interface Certification {
  id: number;
  userId: number;
  name: string;
  issuer: string;
  issueDate: string;
  expiryDate?: string;
}

export interface Experience {
  id: number;
  userId: number;
  companyName: string;
  position: string;
  startDate: string;
  endDate?: string;
  description: string;
  isCurrentJob: boolean;
}

export interface Portfolio {
  id: number;
  userId: number;
  title: string;
  description: string;
  type: PortfolioType;
  url?: string;
  imageUrl?: string;
  technologies: string[];
}

// ============= Job Types =============
export interface JobPosting {
  id: number;
  companyUserId: number;
  companyUserName: string;
  title: string;
  companyName: string;
  location: string;
  jobType: JobType;
  experienceLevel: ExperienceLevel;
  department?: string;
  field?: string;
  description: string;
  qualifications?: string;
  requiredSkills?: string;
  benefits?: string;
  minSalary?: number;
  maxSalary?: number;
  salaryNegotiable?: boolean;
  workingHours?: string;
  isRemotePossible?: boolean;
  contactEmail?: string;
  contactPhone?: string;
  status: JobStatus;
  viewCount?: number;
  applicationCount?: number;
  deadline?: string;
  deadlineDate?: string;
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
  // Legacy field for compatibility
  applicationDeadline?: string;
  createdBy?: number;
}

export interface JobApplication {
  id: number;
  jobPostingId: number;
  applicantId: number;
  coverLetter?: string;
  status: ApplicationStatus;
  appliedAt: string;
  updatedAt: string;
  jobPosting?: JobPosting;
  applicant?: User;
}

export interface JobSearch {
  keyword?: string;
  location?: string;
  jobType?: JobType;
  experienceLevel?: ExperienceLevel;
  salaryMin?: number;
  salaryMax?: number;
  skills?: string[];
}

// ============= Community Types =============
export interface Category {
  id: number;
  name: string;
  description?: string;
  postCount: number;
}

export interface Post {
  id: number;
  title: string;
  content: string;
  categoryId: number;
  categoryName?: string;
  authorId: number;
  authorName?: string;
  imageUrl?: string;
  sentiment?: SentimentType;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isLiked?: boolean;
  createdAt: string;
  updatedAt: string;
  category?: Category;
  author?: User;
  comments?: Comment[];
}

export interface Comment {
  id: number;
  content: string;
  postId: number;
  authorId: number;
  authorName?: string;
  parentId?: number;
  createdAt: string;
  updatedAt: string;
  author?: User;
  replies?: Comment[];
}

// ============= AI Service Types =============
export interface ChatbotRequest {
  userId: string;
  message: string;
}

export interface ChatbotResponse {
  success: boolean;
  message: string;
  data?: {
    user_id: string;
    message: string;
    response: string;
    timestamp: string;
    success: boolean;
  };
  error?: string;
}

export interface InterviewRequest {
  interviewType: 'technical' | 'personality';
  userId?: number;
  jobRole?: string;
  experienceLevel?: string;
  skills?: string[];
}

export interface InterviewQuestion {
  id?: string;
  type?: string;
  question: string;
  category: string;
  difficulty: string;
}

export interface InterviewEvaluation {
  score: number;
  feedback: string;
  strengths: string[];
  improvements: string[];
  exampleAnswer: string;
}

export interface CoverLetterRequest {
  user_id: number;
  company_name: string;
  position: string;
  sections: string[];
  user_info?: Record<string, any>;
}

export interface CoverLetterResponse {
  success: boolean;
  message: string;
  data?: {
    user_id: number;
    company_name: string;
    position: string;
    content: Record<string, string>;
    created_at: string;
  };
  error?: string;
}

export interface TranslationRequest {
  text: string;
  source_language: string;
  target_language: string;
  document_type?: 'resume' | 'cover_letter' | 'email' | 'business' | 'general';
}

export interface TranslationResponse {
  success: boolean;
  message: string;
  data?: {
    originalText: string;
    translatedText: string;
    sourceLanguage: string;
    targetLanguage: string;
    confidence: number;
    documentType?: string;
    createdAt?: string;
  };
  error?: string;
}

export interface ImageGenerationRequest {
  prompt: string;
  user_id: number;
  style?: string;
  size?: string;
}

export interface ImageGenerationResponse {
  success: boolean;
  message: string;
  data?: {
    user_id: number;
    prompt: string;
    image_url: string;
    created_at: string;
  };
  error?: string;
}

// ============= Dashboard Types =============
export interface DashboardData {
  userStats: UserStatistics;
  applicationStats: ApplicationStatistics;
  jobStats: JobStatistics;
  personalInsights: PersonalInsight;
  quickActions: QuickAction[];
}

export interface UserStatistics {
  totalUsers: number;
  newUsers: number;
  activeUsers: number;
  employmentRate: number;
}

export interface ApplicationStatistics {
  totalApplications: number;
  pendingApplications: number;
  acceptedApplications: number;
  rejectedApplications: number;
}

export interface JobStatistics {
  totalJobs: number;
  activeJobs: number;
  newJobs: number;
  popularJobs: JobPosting[];
}

export interface PersonalInsight {
  employmentScore: number;
  skillMatch: number;
  recommendations: string[];
  competitorAnalysis: CompetitorAnalysis;
}

export interface CompetitorAnalysis {
  averageSkillCount: number;
  averageExperience: number;
  topSkills: string[];
  recommendations: string[];
}

export interface QuickAction {
  id: string;
  title: string;
  description: string;
  icon: string;
  url: string;
}

// ============= Certificate Types =============
export interface CertificateRequest {
  id: number;
  userId: number;
  type: CertificateType;
  status: RequestStatus;
  requestedAt: string;
  processedAt?: string;
  notes?: string;
}

// ============= Enums =============
export enum UserType {
  GENERAL = 'GENERAL',
  COMPANY = 'COMPANY',
  ADMIN = 'ADMIN'
}

export enum EmploymentStatus {
  JOB_SEEKING = 'JOB_SEEKING',
  EMPLOYED = 'EMPLOYED',
  STUDENT = 'STUDENT'
}

export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  OTHER = 'OTHER'
}

export enum EducationLevel {
  HIGH_SCHOOL = 'HIGH_SCHOOL',
  ASSOCIATE = 'ASSOCIATE',
  BACHELOR = 'BACHELOR',
  MASTER = 'MASTER',
  DOCTORATE = 'DOCTORATE'
}

export enum SkillCategory {
  PROGRAMMING = 'PROGRAMMING',
  DATABASE = 'DATABASE',
  FRAMEWORK = 'FRAMEWORK',
  TOOL = 'TOOL',
  LANGUAGE = 'LANGUAGE',
  CERTIFICATION = 'CERTIFICATION'
}

export enum SkillLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
  EXPERT = 'EXPERT'
}

export enum PortfolioType {
  WEB = 'WEB',
  MOBILE = 'MOBILE',
  DESKTOP = 'DESKTOP',
  GAME = 'GAME',
  AI_ML = 'AI_ML',
  OTHER = 'OTHER'
}

export enum JobType {
  FULL_TIME = 'FULL_TIME',
  PART_TIME = 'PART_TIME',
  CONTRACT = 'CONTRACT',
  INTERN = 'INTERN',
  FREELANCE = 'FREELANCE'
}

export enum ExperienceLevel {
  ENTRY = 'ENTRY',
  JUNIOR = 'JUNIOR',
  MID = 'MID',
  SENIOR = 'SENIOR',
  LEAD = 'LEAD'
}

export enum JobStatus {
  ACTIVE = 'ACTIVE',
  CLOSED = 'CLOSED',
  DRAFT = 'DRAFT'
}

export enum ApplicationStatus {
  PENDING = 'PENDING',
  REVIEWING = 'REVIEWING',
  INTERVIEW = 'INTERVIEW',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED'
}

export enum SentimentType {
  POSITIVE = 'POSITIVE',
  NEUTRAL = 'NEUTRAL',
  NEGATIVE = 'NEGATIVE'
}

export enum CertificateType {
  COMPLETION = 'COMPLETION',
  ATTENDANCE = 'ATTENDANCE',
  ACHIEVEMENT = 'ACHIEVEMENT'
}

export enum RequestStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  PROCESSED = 'PROCESSED'
}

// ============= Auth Types =============
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  phoneNumber: string;
  userType: UserType;
  academy?: string;
  companyName?: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  data?: {
    access_token: string;
    refresh_token: string;
    token_type: string;
    expires_in: number;
    user: User;
  };
}

export interface OAuth2LoginRequest {
  provider: string;
  code: string;
  state?: string;
}

// ============= Request/Response DTOs =============
export interface CreateJobPostingRequest {
  title: string;
  companyName: string;
  location: string;
  jobType: JobType;
  experienceLevel: ExperienceLevel;
  department?: string;
  description: string;
  minSalary?: number;
  maxSalary?: number;
  qualifications?: string[];
  requiredSkills?: string[];
  applicationDeadline?: string;
}

export interface UpdateJobPostingRequest extends Partial<CreateJobPostingRequest> {
  status?: JobStatus;
}

export interface CreateJobApplicationRequest {
  jobPostingId: number;
  coverLetter?: string;
}

export interface CreatePostRequest {
  title: string;
  content: string;
  categoryId: number;
  imagePrompt?: string;
}

export interface UpdatePostRequest {
  title?: string;
  content?: string;
  categoryId?: number;
}

export interface CreateCommentRequest {
  content: string;
  postId: number;
  parentId?: number;
}

export interface UpdateUserProfileRequest {
  age?: number;
  gender?: Gender;
  location?: string;
  desiredPosition?: string;
  desiredCompany?: string;
}

export interface CreateEducationRequest {
  degree: EducationLevel;
  school: string;
  major: string;
  graduationYear: number;
  gpa?: number;
  maxGpa?: number;
}

export interface CreateSkillRequest {
  name: string;
  category: SkillCategory;
  level: SkillLevel;
}

export interface CreateCertificationRequest {
  name: string;
  issuer: string;
  issueDate: string;
  expiryDate?: string;
}

export interface CreateExperienceRequest {
  companyName: string;
  position: string;
  startDate: string;
  endDate?: string;
  description: string;
  isCurrentJob: boolean;
}

export interface CreatePortfolioRequest {
  title: string;
  description: string;
  type: PortfolioType;
  url?: string;
  imageUrl?: string;
  technologies: string[];
}

export interface CreateCertificateRequestDto {
  type: CertificateType;
  notes?: string;
}