import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Loader2, Languages, Copy, Download, History } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { translationAPI } from '@/services/api/translation';

interface TranslationHistory {
  id: number;
  sourceText: string;
  translatedText: string;
  sourceLanguage: string;
  targetLanguage: string;
  documentType: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  createdAt: string;
  characterCount: number;
}

const LANGUAGES = [
  { code: 'ko', name: '한국어' },
  { code: 'en', name: 'English' },
  { code: 'ja', name: '日本語' },
  { code: 'zh', name: '中文' },
  { code: 'es', name: 'Español' },
  { code: 'fr', name: 'Français' },
  { code: 'de', name: 'Deutsch' },
];

const DOCUMENT_TYPES = [
  { value: 'general', label: '일반 문서' },
  { value: 'resume', label: '이력서' },
  { value: 'cover_letter', label: '자기소개서' },
  { value: 'business', label: '비즈니스 문서' },
];

const TranslationPage: React.FC = () => {
  const [sourceText, setSourceText] = useState('');
  const [translatedText, setTranslatedText] = useState('');
  const [sourceLanguage, setSourceLanguage] = useState('ko');
  const [targetLanguage, setTargetLanguage] = useState('en');
  const [documentType, setDocumentType] = useState('general');
  const [isTranslating, setIsTranslating] = useState(false);
  const [history, setHistory] = useState<TranslationHistory[]>([]);
  const [showHistory, setShowHistory] = useState(false);

  const { toast } = useToast();

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    try {
      const response = await translationAPI.getHistory();
      setHistory(response.data);
    } catch (error) {
      console.error('번역 기록 로드 실패:', error);
    }
  };

  const handleTranslate = async () => {
    if (!sourceText.trim()) {
      toast({
        title: '오류',
        description: '번역할 텍스트를 입력해주세요.',
        variant: 'destructive',
      });
      return;
    }

    if (sourceLanguage === targetLanguage) {
      toast({
        title: '오류',
        description: '원본 언어와 번역 언어가 같습니다.',
        variant: 'destructive',
      });
      return;
    }

    setIsTranslating(true);
    try {
      const response = await translationAPI.translate({
        sourceText,
        sourceLanguage,
        targetLanguage,
        documentType,
      });

      if (response.data.status === 'COMPLETED') {
        setTranslatedText(response.data.translatedText);
        toast({
          title: '번역 완료',
          description: '번역이 성공적으로 완료되었습니다.',
        });
        loadHistory(); // 기록 새로고침
      } else {
        throw new Error(response.data.errorMessage || '번역 실패');
      }
    } catch (error: any) {
      toast({
        title: '번역 실패',
        description: error.response?.data?.message || '번역 중 오류가 발생했습니다.',
        variant: 'destructive',
      });
    } finally {
      setIsTranslating(false);
    }
  };

  const handleCopy = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      toast({
        title: '복사 완료',
        description: '텍스트가 클립보드에 복사되었습니다.',
      });
    } catch (error) {
      toast({
        title: '복사 실패',
        description: '텍스트 복사에 실패했습니다.',
        variant: 'destructive',
      });
    }
  };

  const handleSwapLanguages = () => {
    const tempLang = sourceLanguage;
    setSourceLanguage(targetLanguage);
    setTargetLanguage(tempLang);

    // 텍스트도 교체
    const tempText = sourceText;
    setSourceText(translatedText);
    setTranslatedText(tempText);
  };

  const handleHistorySelect = (item: TranslationHistory) => {
    setSourceText(item.sourceText);
    setTranslatedText(item.translatedText);
    setSourceLanguage(item.sourceLanguage);
    setTargetLanguage(item.targetLanguage);
    setDocumentType(item.documentType);
    setShowHistory(false);
  };

  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold flex items-center gap-2">
            <Languages className="h-8 w-8" />
            AI 번역 서비스
          </h1>
          <p className="text-muted-foreground mt-2">
            다양한 언어를 정확하고 빠르게 번역해보세요
          </p>
        </div>
        <Button
          variant="outline"
          onClick={() => setShowHistory(!showHistory)}
        >
          <History className="h-4 w-4 mr-2" />
          번역 기록
        </Button>
      </div>

      {showHistory && (
        <Card>
          <CardHeader>
            <CardTitle>번역 기록</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {history.length === 0 ? (
                <p className="text-center text-muted-foreground py-8">
                  번역 기록이 없습니다.
                </p>
              ) : (
                history.map((item) => (
                  <div
                    key={item.id}
                    className="border rounded-lg p-4 cursor-pointer hover:bg-muted/50"
                    onClick={() => handleHistorySelect(item)}
                  >
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <Badge variant="secondary">
                          {LANGUAGES.find(l => l.code === item.sourceLanguage)?.name} → {LANGUAGES.find(l => l.code === item.targetLanguage)?.name}
                        </Badge>
                        <Badge variant="outline">
                          {DOCUMENT_TYPES.find(t => t.value === item.documentType)?.label}
                        </Badge>
                        <Badge
                          variant={item.status === 'COMPLETED' ? 'default' : 'destructive'}
                        >
                          {item.status}
                        </Badge>
                      </div>
                      <span className="text-sm text-muted-foreground">
                        {new Date(item.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                    <p className="text-sm truncate">
                      {item.sourceText.substring(0, 100)}
                      {item.sourceText.length > 100 && '...'}
                    </p>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid md:grid-cols-2 gap-6">
        {/* 원본 텍스트 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                원본 텍스트
                <Select value={sourceLanguage} onValueChange={setSourceLanguage}>
                  <SelectTrigger className="w-32">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {LANGUAGES.map((lang) => (
                      <SelectItem key={lang.code} value={lang.code}>
                        {lang.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={handleSwapLanguages}
                className="text-xs"
              >
                언어 교체
              </Button>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <Textarea
              value={sourceText}
              onChange={(e) => setSourceText(e.target.value)}
              placeholder="번역할 텍스트를 입력하세요..."
              className="min-h-[200px]"
              maxLength={5000}
            />
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">
                {sourceText.length}/5,000자
              </span>
              {sourceText && (
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleCopy(sourceText)}
                >
                  <Copy className="h-4 w-4 mr-2" />
                  복사
                </Button>
              )}
            </div>
          </CardContent>
        </Card>

        {/* 번역된 텍스트 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              번역된 텍스트
              <Select value={targetLanguage} onValueChange={setTargetLanguage}>
                <SelectTrigger className="w-32">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {LANGUAGES.map((lang) => (
                    <SelectItem key={lang.code} value={lang.code}>
                      {lang.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <Textarea
              value={translatedText}
              readOnly
              placeholder="번역 결과가 여기에 표시됩니다..."
              className="min-h-[200px] bg-muted/50"
            />
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">
                {translatedText ? `${translatedText.length}자` : '0자'}
              </span>
              {translatedText && (
                <div className="flex gap-2">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleCopy(translatedText)}
                  >
                    <Copy className="h-4 w-4 mr-2" />
                    복사
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      const element = document.createElement('a');
                      const file = new Blob([translatedText], { type: 'text/plain' });
                      element.href = URL.createObjectURL(file);
                      element.download = 'translation.txt';
                      document.body.appendChild(element);
                      element.click();
                      document.body.removeChild(element);
                    }}
                  >
                    <Download className="h-4 w-4 mr-2" />
                    다운로드
                  </Button>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 번역 설정 및 실행 */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div>
                <label className="text-sm font-medium">문서 유형</label>
                <Select value={documentType} onValueChange={setDocumentType}>
                  <SelectTrigger className="w-40 mt-1">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {DOCUMENT_TYPES.map((type) => (
                      <SelectItem key={type.value} value={type.value}>
                        {type.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <Button
              onClick={handleTranslate}
              disabled={isTranslating || !sourceText.trim()}
              size="lg"
            >
              {isTranslating ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  번역 중...
                </>
              ) : (
                <>
                  <Languages className="h-4 w-4 mr-2" />
                  번역하기
                </>
              )}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default TranslationPage;