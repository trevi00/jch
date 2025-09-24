import { api } from './index';

export interface CreateTicketRequest {
  subject: string;
  description: string;
  category: string;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
}

export interface SupportTicket {
  id: number;
  subject: string;
  description: string;
  category: string;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  assignedAdminName?: string;
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
  messageCount: number;
  satisfactionRating?: number;
  satisfactionFeedback?: string;
}

export interface SupportMessage {
  id: number;
  ticketId: number;
  senderName: string;
  message: string;
  isFromSupport: boolean;
  isInternalNote: boolean;
  attachmentUrl?: string;
  createdAt: string;
}

export interface SendMessageRequest {
  message: string;
  attachmentUrl?: string;
}

export interface TicketStats {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  closedTickets: number;
  averageResponseTime: number;
  satisfactionScore: number;
}

export const supportAPI = {
  // 티켓 관리
  getTickets: (status?: string, page = 0, size = 20) =>
    api.get<SupportTicket[]>('/api/support/tickets', {
      params: { status, page, size }
    }),

  getTicket: (id: number) =>
    api.get<SupportTicket>(`/api/support/tickets/${id}`),

  createTicket: (data: CreateTicketRequest) =>
    api.post<SupportTicket>('/api/support/tickets', data),

  updateTicketStatus: (id: number, status: string) =>
    api.put<SupportTicket>(`/api/support/tickets/${id}/status`, { status }),

  closeTicket: (id: number) =>
    api.put<SupportTicket>(`/api/support/tickets/${id}/close`),

  reopenTicket: (id: number) =>
    api.put<SupportTicket>(`/api/support/tickets/${id}/reopen`),

  // 메시지 관리
  getMessages: (ticketId: number) =>
    api.get<SupportMessage[]>(`/api/support/tickets/${ticketId}/messages`),

  sendMessage: (ticketId: number, data: SendMessageRequest) =>
    api.post<SupportMessage>(`/api/support/tickets/${ticketId}/messages`, data),

  // 만족도 평가
  submitSatisfactionRating: (ticketId: number, rating: number, feedback?: string) =>
    api.post(`/api/support/tickets/${ticketId}/satisfaction`, {
      rating,
      feedback
    }),

  // 통계 및 분석
  getTicketStats: () =>
    api.get<TicketStats>('/api/support/stats'),

  getMyTicketStats: () =>
    api.get<TicketStats>('/api/support/my-stats'),

  // 카테고리 관리
  getCategories: () =>
    api.get<{ value: string; label: string; description?: string }[]>('/api/support/categories'),

  // FAQ
  getFAQ: (category?: string) =>
    api.get<{
      id: number;
      question: string;
      answer: string;
      category: string;
      viewCount: number;
      helpful: number;
    }[]>('/api/support/faq', { params: { category } }),

  // 파일 업로드 (첨부파일용)
  uploadAttachment: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<{ fileUrl: string }>('/api/support/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  // 검색
  searchTickets: (query: string, category?: string, status?: string) =>
    api.get<SupportTicket[]>('/api/support/search', {
      params: { q: query, category, status }
    }),
};