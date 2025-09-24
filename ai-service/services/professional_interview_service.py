"""
전문적인 AI 면접 서비스
GPT-4 기반의 고급 면접 질문 생성 및 피드백 시스템
"""

from typing import List, Dict, Any, Optional
import logging
from datetime import datetime
import json
from services.enhanced_openai_service import enhanced_openai_service

logger = logging.getLogger(__name__)

class ProfessionalInterviewService:
    """전문적인 AI 면접 서비스"""
    
    def __init__(self):
        self.question_templates = self._load_question_templates()
        self.evaluation_criteria = self._load_evaluation_criteria()
        
    def _load_question_templates(self) -> Dict[str, Any]:
        """면접 질문 템플릿 로드"""
        return {
            "technical": {
                "junior": {
                    "system_design": [
                        "간단한 웹 애플리케이션의 구조를 설계해보세요",
                        "RESTful API 설계 원칙에 대해 설명해주세요",
                        "데이터베이스 정규화의 장단점을 설명해주세요"
                    ],
                    "coding": [
                        "자주 사용하는 자료구조와 그 활용 사례를 설명해주세요",
                        "시간 복잡도와 공간 복잡도의 차이를 설명해주세요",
                        "최근 작성한 코드 중 자랑스러운 부분을 설명해주세요"
                    ],
                    "problem_solving": [
                        "디버깅 과정에서 어려웠던 경험과 해결 방법을 공유해주세요",
                        "성능 최적화를 위해 시도한 방법들을 설명해주세요",
                        "코드 리뷰에서 받은 피드백 중 기억에 남는 것은?"
                    ]
                },
                "mid": {
                    "system_design": [
                        "마이크로서비스 아키텍처의 장단점과 적용 사례를 설명해주세요",
                        "대용량 트래픽을 처리하는 시스템을 설계해보세요",
                        "이벤트 기반 아키텍처와 요청-응답 패턴의 차이점을 설명해주세요"
                    ],
                    "coding": [
                        "디자인 패턴 중 실무에서 활용한 경험을 공유해주세요",
                        "동시성 문제를 해결한 경험을 설명해주세요",
                        "테스트 주도 개발(TDD)의 실제 적용 경험을 공유해주세요"
                    ],
                    "leadership": [
                        "주니어 개발자를 멘토링한 경험을 공유해주세요",
                        "기술적 의사결정을 주도한 경험을 설명해주세요",
                        "팀 내 기술 공유 문화를 만든 경험이 있나요?"
                    ]
                },
                "senior": {
                    "architecture": [
                        "레거시 시스템을 현대화한 경험을 공유해주세요",
                        "기술 부채를 관리하고 해결한 전략을 설명해주세요",
                        "클라우드 마이그레이션 전략과 경험을 공유해주세요"
                    ],
                    "leadership": [
                        "기술 조직의 성장 전략을 어떻게 수립하시나요?",
                        "개발 문화 개선을 위해 시도한 방법들을 공유해주세요",
                        "기술 선택 시 고려하는 요소들과 의사결정 프로세스를 설명해주세요"
                    ],
                    "business": [
                        "기술과 비즈니스 가치를 연결한 경험을 공유해주세요",
                        "ROI를 고려한 기술 투자 결정 경험을 설명해주세요",
                        "제품 전략과 기술 전략을 정렬한 경험을 공유해주세요"
                    ]
                }
            },
            "behavioral": {
                "teamwork": [
                    "팀 내 갈등을 해결한 경험을 STAR 방법으로 설명해주세요",
                    "다양한 배경의 팀원들과 협업한 경험을 공유해주세요",
                    "의견 차이가 있을 때 어떻게 조율하시나요?"
                ],
                "problem_solving": [
                    "예상치 못한 문제를 창의적으로 해결한 경험을 공유해주세요",
                    "실패에서 배운 가장 중요한 교훈은 무엇인가요?",
                    "압박감 속에서 의사결정을 내린 경험을 설명해주세요"
                ],
                "growth": [
                    "최근 1년간 가장 크게 성장한 부분은 무엇인가요?",
                    "새로운 기술을 학습하는 나만의 방법은?",
                    "커리어 목표와 그를 위한 구체적인 계획을 공유해주세요"
                ]
            }
        }
    
    def _load_evaluation_criteria(self) -> Dict[str, List[str]]:
        """평가 기준 로드"""
        return {
            "technical": [
                "기술적 정확성",
                "문제 해결 접근법",
                "코드 품질 의식",
                "시스템 설계 능력",
                "최신 기술 트렌드 이해"
            ],
            "behavioral": [
                "의사소통 능력",
                "팀워크와 협업",
                "리더십 잠재력",
                "학습 능력과 성장 마인드셋",
                "문제 해결 능력"
            ],
            "cultural_fit": [
                "회사 가치관과의 일치",
                "업무 태도와 책임감",
                "적응력과 유연성",
                "열정과 동기부여",
                "장기적 비전"
            ]
        }
    
    async def generate_professional_questions(
        self,
        position: str,
        level: str,
        company_type: str,
        required_skills: List[str],
        interview_type: str = "mixed"
    ) -> Dict[str, Any]:
        """전문적인 면접 질문 생성"""
        
        system_prompt = f"""
        당신은 {company_type} 기업의 수석 면접관입니다.
        {position} 포지션의 {level} 레벨 지원자를 위한 맞춤형 면접 질문을 생성해주세요.
        
        필수 기술: {', '.join(required_skills)}
        면접 유형: {interview_type}
        
        요구사항:
        1. 실무 중심의 구체적인 질문
        2. 지원자의 경험과 역량을 깊이 있게 평가할 수 있는 질문
        3. STAR 방법론을 활용할 수 있는 행동 질문 포함
        4. 기술적 깊이와 비즈니스 이해도를 모두 평가
        5. 각 질문에 대한 평가 포인트 제시
        
        JSON 형식으로 응답해주세요:
        {{
            "questions": [
                {{
                    "id": 1,
                    "category": "technical/behavioral/situational",
                    "question": "질문 내용",
                    "difficulty": "easy/medium/hard",
                    "evaluation_points": ["평가 포인트 1", "평가 포인트 2"],
                    "follow_up_questions": ["후속 질문 1", "후속 질문 2"],
                    "expected_answer_keywords": ["키워드1", "키워드2"],
                    "time_allocation": "예상 소요 시간(분)"
                }}
            ],
            "interview_structure": {{
                "total_duration": "총 면접 시간(분)",
                "sections": [
                    {{"name": "섹션명", "duration": "시간", "question_count": 개수}}
                ]
            }},
            "evaluation_framework": {{
                "criteria": ["평가 기준"],
                "scoring_guide": "점수 가이드"
            }}
        }}
        """
        
        try:
            result = await enhanced_openai_service.generate_completion_with_retry(
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": f"다음 조건에 맞는 면접 질문을 생성해주세요:\n포지션: {position}\n레벨: {level}\n회사: {company_type}\n핵심 기술: {', '.join(required_skills)}"}
                ],
                model="gpt-4-turbo-preview",
                temperature=0.7,
                max_tokens=2000
            )
            
            # JSON 파싱
            response_text = result.get('content', '{}')
            try:
                questions_data = json.loads(response_text)
            except json.JSONDecodeError:
                # JSON 파싱 실패 시 기본 구조 생성
                questions_data = self._create_fallback_questions(position, level, required_skills)
            
            # 메타데이터 추가
            questions_data['metadata'] = {
                'generated_at': datetime.now().isoformat(),
                'position': position,
                'level': level,
                'company_type': company_type,
                'required_skills': required_skills,
                'quality_score': result.get('quality_score', 0)
            }
            
            return questions_data
            
        except Exception as e:
            logger.error(f"전문 면접 질문 생성 실패: {e}")
            return self._create_fallback_questions(position, level, required_skills)
    
    async def evaluate_answer(
        self,
        question: str,
        answer: str,
        expected_points: List[str],
        evaluation_criteria: List[str]
    ) -> Dict[str, Any]:
        """면접 답변 평가"""
        
        system_prompt = """
        당신은 경험이 풍부한 기술 면접 평가관입니다.
        지원자의 답변을 다음 기준에 따라 상세히 평가해주세요:
        
        평가 차원:
        1. 기술적 정확성 (Technical Accuracy)
        2. 문제 해결 접근법 (Problem-Solving Approach)
        3. 의사소통 명확성 (Communication Clarity)
        4. 실무 경험 반영도 (Practical Experience)
        5. 사고의 깊이와 창의성 (Depth and Creativity)
        
        점수 기준:
        - 10-9점: 탁월함 (기대를 뛰어넘는 수준)
        - 8-7점: 우수함 (기대 수준 충족 및 추가 강점)
        - 6-5점: 양호함 (기본 요구사항 충족)
        - 4-3점: 보통 (일부 부족하지만 잠재력 있음)
        - 2-1점: 미흡 (상당한 개선 필요)
        
        JSON 형식으로 평가 결과를 제공해주세요:
        {
            "overall_score": 0-10,
            "dimension_scores": {
                "technical_accuracy": 0-10,
                "problem_solving": 0-10,
                "communication": 0-10,
                "practical_experience": 0-10,
                "creativity": 0-10
            },
            "strengths": ["강점 1", "강점 2"],
            "improvements": ["개선점 1", "개선점 2"],
            "specific_feedback": "구체적인 피드백",
            "follow_up_suggestions": ["추가 학습 제안"],
            "hiring_recommendation": "strong_yes/yes/maybe/no"
        }
        """
        
        user_prompt = f"""
        질문: {question}
        
        지원자 답변: {answer}
        
        평가 포인트: {', '.join(expected_points)}
        
        평가 기준: {', '.join(evaluation_criteria)}
        """
        
        try:
            result = await enhanced_openai_service.generate_completion_with_retry(
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                model="gpt-4-turbo-preview",
                temperature=0.3,
                max_tokens=1500
            )
            
            evaluation_text = result.get('content', '{}')
            
            try:
                evaluation = json.loads(evaluation_text)
            except json.JSONDecodeError:
                # 파싱 실패 시 텍스트 분석으로 기본 평가 생성
                evaluation = self._parse_text_evaluation(evaluation_text)
            
            # 추가 분석 정보
            evaluation['analysis_metadata'] = {
                'evaluated_at': datetime.now().isoformat(),
                'model_used': result.get('model', 'unknown'),
                'confidence': result.get('quality_score', 0)
            }
            
            return evaluation
            
        except Exception as e:
            logger.error(f"답변 평가 실패: {e}")
            return {
                "overall_score": 5,
                "feedback": "평가 중 오류가 발생했습니다. 다시 시도해주세요.",
                "error": str(e)
            }
    
    async def generate_interview_report(
        self,
        candidate_name: str,
        position: str,
        questions_and_answers: List[Dict[str, Any]],
        interview_duration: int
    ) -> Dict[str, Any]:
        """종합 면접 리포트 생성"""
        
        system_prompt = """
        당신은 인재 채용 전문가입니다.
        면접 결과를 바탕으로 종합적인 평가 리포트를 작성해주세요.
        
        리포트 구성:
        1. 종합 평가 요약 (Executive Summary)
        2. 역량별 상세 평가
        3. 강점과 개발 필요 영역
        4. 문화적 적합성 평가
        5. 채용 추천 및 근거
        6. 온보딩 제안사항
        """
        
        qa_summary = "\n".join([
            f"Q{i+1}: {qa['question']}\nA: {qa['answer']}\n평가: {qa.get('score', 'N/A')}/10"
            for i, qa in enumerate(questions_and_answers)
        ])
        
        user_prompt = f"""
        지원자: {candidate_name}
        포지션: {position}
        면접 시간: {interview_duration}분
        
        면접 내용:
        {qa_summary}
        
        종합 평가 리포트를 작성해주세요.
        """
        
        try:
            result = await enhanced_openai_service.generate_completion_with_retry(
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": user_prompt}
                ],
                model="gpt-4-turbo-preview",
                temperature=0.5,
                max_tokens=2500
            )
            
            report = {
                "candidate": candidate_name,
                "position": position,
                "interview_date": datetime.now().isoformat(),
                "duration_minutes": interview_duration,
                "report_content": result.get('content', ''),
                "summary_scores": self._calculate_summary_scores(questions_and_answers),
                "recommendation": self._determine_recommendation(questions_and_answers),
                "metadata": {
                    "generated_at": datetime.now().isoformat(),
                    "model": result.get('model', 'unknown'),
                    "quality_score": result.get('quality_score', 0)
                }
            }
            
            return report
            
        except Exception as e:
            logger.error(f"면접 리포트 생성 실패: {e}")
            return {
                "error": str(e),
                "message": "리포트 생성 중 오류가 발생했습니다."
            }
    
    def _create_fallback_questions(
        self,
        position: str,
        level: str,
        skills: List[str]
    ) -> Dict[str, Any]:
        """폴백 질문 생성"""
        base_questions = self.question_templates.get("technical", {}).get(level, {})
        
        questions = []
        question_id = 1
        
        for category, category_questions in base_questions.items():
            for q in category_questions[:2]:  # 각 카테고리에서 2개씩
                questions.append({
                    "id": question_id,
                    "category": category,
                    "question": q,
                    "difficulty": "medium",
                    "evaluation_points": ["논리적 사고", "실무 경험", "기술 이해도"],
                    "time_allocation": 5
                })
                question_id += 1
        
        return {
            "questions": questions,
            "interview_structure": {
                "total_duration": 60,
                "sections": [
                    {"name": "기술 면접", "duration": 40, "question_count": 6},
                    {"name": "인성 면접", "duration": 20, "question_count": 4}
                ]
            }
        }
    
    def _parse_text_evaluation(self, text: str) -> Dict[str, Any]:
        """텍스트 평가를 구조화된 데이터로 변환"""
        # 간단한 텍스트 파싱 로직
        score = 5
        if "excellent" in text.lower() or "outstanding" in text.lower():
            score = 9
        elif "good" in text.lower() or "well" in text.lower():
            score = 7
        elif "poor" in text.lower() or "weak" in text.lower():
            score = 3
        
        return {
            "overall_score": score,
            "feedback": text,
            "parsed_from_text": True
        }
    
    def _calculate_summary_scores(self, qa_list: List[Dict]) -> Dict[str, float]:
        """종합 점수 계산"""
        if not qa_list:
            return {"average": 0, "min": 0, "max": 0}
        
        scores = [qa.get('score', 5) for qa in qa_list]
        return {
            "average": sum(scores) / len(scores),
            "min": min(scores),
            "max": max(scores),
            "total_questions": len(qa_list)
        }
    
    def _determine_recommendation(self, qa_list: List[Dict]) -> str:
        """채용 추천 결정"""
        avg_score = self._calculate_summary_scores(qa_list)["average"]
        
        if avg_score >= 8:
            return "강력 추천"
        elif avg_score >= 6.5:
            return "추천"
        elif avg_score >= 5:
            return "조건부 추천"
        else:
            return "재검토 필요"

# 싱글톤 인스턴스
professional_interview_service = ProfessionalInterviewService()