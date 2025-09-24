import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import {
  HelpCircle,
  Plus,
  MessageSquare,
  Clock,
  CheckCircle,
  XCircle,
  AlertTriangle,
  Send,
  Loader2
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { supportAPI } from '@/services/api/support';

interface SupportTicket {
  id: number;
  subject: string;
  description: string;
  category: string;
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  createdAt: string;
  updatedAt: string;
  messageCount: number;
}

interface SupportMessage {
  id: number;
  ticketId: number;
  senderName: string;
  message: string;
  isFromSupport: boolean;
  createdAt: string;
}

const CATEGORIES = [
  { value: 'technical', label: '기술적 문제' },
  { value: 'billing', label: '결제 관련' },
  { value: 'account', label: '계정 관련' },
  { value: 'general', label: '일반 문의' },
  { value: 'feature', label: '기능 요청' },
];

const PRIORITIES = [
  { value: 'LOW', label: '낮음', color: 'bg-gray-500' },
  { value: 'NORMAL', label: '보통', color: 'bg-blue-500' },
  { value: 'HIGH', label: '높음', color: 'bg-orange-500' },
  { value: 'URGENT', label: '긴급', color: 'bg-red-500' },
];

const STATUS_INFO = {
  OPEN: { label: '접수됨', icon: Clock, color: 'bg-blue-500' },
  IN_PROGRESS: { label: '처리중', icon: MessageSquare, color: 'bg-yellow-500' },
  RESOLVED: { label: '해결됨', icon: CheckCircle, color: 'bg-green-500' },
  CLOSED: { label: '종료됨', icon: XCircle, color: 'bg-gray-500' },
};

const SupportPage: React.FC = () => {
  const [tickets, setTickets] = useState<SupportTicket[]>([]);
  const [selectedTicket, setSelectedTicket] = useState<SupportTicket | null>(null);
  const [messages, setMessages] = useState<SupportMessage[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSending, setIsSending] = useState(false);

  // 새 티켓 폼 상태
  const [newTicketForm, setNewTicketForm] = useState({
    subject: '',
    description: '',
    category: 'general',
    priority: 'NORMAL' as const,
  });

  const { toast } = useToast();

  useEffect(() => {
    loadTickets();
  }, []);

  useEffect(() => {
    if (selectedTicket) {
      loadMessages(selectedTicket.id);
    }
  }, [selectedTicket]);

  const loadTickets = async () => {
    try {
      setIsLoading(true);
      const response = await supportAPI.getTickets();
      setTickets(response.data);
    } catch (error: any) {
      toast({
        title: '오류',
        description: '티켓 목록을 불러오는데 실패했습니다.',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  const loadMessages = async (ticketId: number) => {
    try {
      const response = await supportAPI.getMessages(ticketId);
      setMessages(response.data);
    } catch (error: any) {
      toast({
        title: '오류',
        description: '메시지를 불러오는데 실패했습니다.',
        variant: 'destructive',
      });
    }
  };

  const handleCreateTicket = async () => {
    if (!newTicketForm.subject.trim() || !newTicketForm.description.trim()) {
      toast({
        title: '오류',
        description: '제목과 내용을 모두 입력해주세요.',
        variant: 'destructive',
      });
      return;
    }

    try {
      await supportAPI.createTicket(newTicketForm);
      toast({
        title: '티켓 생성 완료',
        description: '고객지원 요청이 성공적으로 접수되었습니다.',
      });

      setIsCreateDialogOpen(false);
      setNewTicketForm({
        subject: '',
        description: '',
        category: 'general',
        priority: 'NORMAL',
      });
      loadTickets();
    } catch (error: any) {
      toast({
        title: '티켓 생성 실패',
        description: error.response?.data?.message || '티켓 생성에 실패했습니다.',
        variant: 'destructive',
      });
    }
  };

  const handleSendMessage = async () => {
    if (!newMessage.trim() || !selectedTicket) return;

    try {
      setIsSending(true);
      await supportAPI.sendMessage(selectedTicket.id, { message: newMessage });
      setNewMessage('');
      loadMessages(selectedTicket.id);
      loadTickets(); // 티켓 목록 새로고침 (메시지 수 업데이트)
    } catch (error: any) {
      toast({
        title: '메시지 전송 실패',
        description: '메시지 전송에 실패했습니다.',
        variant: 'destructive',
      });
    } finally {
      setIsSending(false);
    }
  };

  const getPriorityColor = (priority: string) => {
    return PRIORITIES.find(p => p.value === priority)?.color || 'bg-gray-500';
  };

  const getCategoryLabel = (category: string) => {
    return CATEGORIES.find(c => c.value === category)?.label || category;
  };

  return (
    <div className="container mx-auto p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <HelpCircle className="h-8 w-8" />
            고객 지원
          </h1>
          <p className="text-muted-foreground mt-2">
            문제 해결을 위해 도움을 요청하세요
          </p>
        </div>

        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="h-4 w-4 mr-2" />
              새 티켓 생성
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[600px]">
            <DialogHeader>
              <DialogTitle>새 고객지원 요청</DialogTitle>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <label className="text-sm font-medium">제목</label>
                <Input
                  value={newTicketForm.subject}
                  onChange={(e) => setNewTicketForm(prev => ({ ...prev, subject: e.target.value }))}
                  placeholder="문제에 대한 간단한 제목을 입력하세요"
                  className="mt-1"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium">카테고리</label>
                  <Select
                    value={newTicketForm.category}
                    onValueChange={(value) => setNewTicketForm(prev => ({ ...prev, category: value }))}
                  >
                    <SelectTrigger className="mt-1">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {CATEGORIES.map((category) => (
                        <SelectItem key={category.value} value={category.value}>
                          {category.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <label className="text-sm font-medium">우선순위</label>
                  <Select
                    value={newTicketForm.priority}
                    onValueChange={(value: any) => setNewTicketForm(prev => ({ ...prev, priority: value }))}
                  >
                    <SelectTrigger className="mt-1">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {PRIORITIES.map((priority) => (
                        <SelectItem key={priority.value} value={priority.value}>
                          {priority.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              <div>
                <label className="text-sm font-medium">상세 내용</label>
                <Textarea
                  value={newTicketForm.description}
                  onChange={(e) => setNewTicketForm(prev => ({ ...prev, description: e.target.value }))}
                  placeholder="문제 상황을 자세히 설명해주세요"
                  className="mt-1 min-h-[120px]"
                />
              </div>

              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setIsCreateDialogOpen(false)}>
                  취소
                </Button>
                <Button onClick={handleCreateTicket}>
                  티켓 생성
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        {/* 티켓 목록 */}
        <div className="lg:col-span-1">
          <Card>
            <CardHeader>
              <CardTitle>내 티켓</CardTitle>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="flex justify-center py-8">
                  <Loader2 className="h-8 w-8 animate-spin" />
                </div>
              ) : tickets.length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">
                  <HelpCircle className="h-12 w-12 mx-auto mb-4 opacity-50" />
                  <p>아직 생성한 티켓이 없습니다.</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {tickets.map((ticket) => {
                    const StatusIcon = STATUS_INFO[ticket.status].icon;
                    return (
                      <div
                        key={ticket.id}
                        className={`p-3 border rounded-lg cursor-pointer hover:bg-muted/50 ${
                          selectedTicket?.id === ticket.id ? 'ring-2 ring-primary' : ''
                        }`}
                        onClick={() => setSelectedTicket(ticket)}
                      >
                        <div className="flex items-start justify-between mb-2">
                          <h3 className="font-medium text-sm truncate">
                            {ticket.subject}
                          </h3>
                          <div className={`w-2 h-2 rounded-full ${getPriorityColor(ticket.priority)} ml-2 mt-1`} />
                        </div>

                        <div className="flex items-center gap-2 mb-2">
                          <Badge variant="outline" className="text-xs">
                            {getCategoryLabel(ticket.category)}
                          </Badge>
                          <div className="flex items-center gap-1">
                            <StatusIcon className={`h-3 w-3 ${STATUS_INFO[ticket.status].color}`} />
                            <span className="text-xs text-muted-foreground">
                              {STATUS_INFO[ticket.status].label}
                            </span>
                          </div>
                        </div>

                        <div className="flex items-center justify-between text-xs text-muted-foreground">
                          <span>{new Date(ticket.createdAt).toLocaleDateString()}</span>
                          {ticket.messageCount > 0 && (
                            <span className="flex items-center gap-1">
                              <MessageSquare className="h-3 w-3" />
                              {ticket.messageCount}
                            </span>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* 티켓 상세 */}
        <div className="lg:col-span-2">
          {selectedTicket ? (
            <Card className="h-[600px] flex flex-col">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      {selectedTicket.subject}
                      <div className={`w-2 h-2 rounded-full ${getPriorityColor(selectedTicket.priority)}`} />
                    </CardTitle>
                    <div className="flex items-center gap-2 mt-2">
                      <Badge variant="outline">
                        {getCategoryLabel(selectedTicket.category)}
                      </Badge>
                      <div className="flex items-center gap-1">
                        {React.createElement(STATUS_INFO[selectedTicket.status].icon, {
                          className: `h-4 w-4 ${STATUS_INFO[selectedTicket.status].color}`
                        })}
                        <span className="text-sm">
                          {STATUS_INFO[selectedTicket.status].label}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </CardHeader>

              <CardContent className="flex-1 flex flex-col">
                {/* 초기 설명 */}
                <div className="bg-muted/50 rounded-lg p-3 mb-4">
                  <p className="text-sm">{selectedTicket.description}</p>
                  <p className="text-xs text-muted-foreground mt-2">
                    {new Date(selectedTicket.createdAt).toLocaleString()}
                  </p>
                </div>

                {/* 메시지 목록 */}
                <div className="flex-1 overflow-y-auto space-y-3 mb-4">
                  {messages.map((message) => (
                    <div
                      key={message.id}
                      className={`flex ${message.isFromSupport ? 'justify-start' : 'justify-end'}`}
                    >
                      <div
                        className={`max-w-[80%] rounded-lg p-3 ${
                          message.isFromSupport
                            ? 'bg-blue-100 text-blue-900'
                            : 'bg-primary text-primary-foreground'
                        }`}
                      >
                        <div className="flex items-center justify-between mb-1">
                          <span className="text-xs font-medium">
                            {message.isFromSupport ? '고객지원팀' : '나'}
                          </span>
                          <span className="text-xs opacity-75">
                            {new Date(message.createdAt).toLocaleString()}
                          </span>
                        </div>
                        <p className="text-sm">{message.message}</p>
                      </div>
                    </div>
                  ))}
                </div>

                {/* 메시지 입력 */}
                {selectedTicket.status !== 'CLOSED' && (
                  <div className="flex gap-2">
                    <Input
                      value={newMessage}
                      onChange={(e) => setNewMessage(e.target.value)}
                      placeholder="메시지를 입력하세요..."
                      onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
                    />
                    <Button
                      onClick={handleSendMessage}
                      disabled={isSending || !newMessage.trim()}
                    >
                      {isSending ? (
                        <Loader2 className="h-4 w-4 animate-spin" />
                      ) : (
                        <Send className="h-4 w-4" />
                      )}
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
          ) : (
            <Card className="h-[600px] flex items-center justify-center">
              <div className="text-center text-muted-foreground">
                <MessageSquare className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p>티켓을 선택하여 대화를 시작하세요</p>
              </div>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};

export default SupportPage;