from typing import List, Dict, Any
import json
import logging
from datetime import datetime

from models.schemas import (
    InterviewQuestion, InterviewFeedback, InterviewType
)
from services.openai_service import openai_service

logger = logging.getLogger(__name__)

class EnhancedInterviewService:
    """전문성이 강화된 AI 면접 서비스"""
    
    def __init__(self):
        # 기술별 전문 질문 카테고리
        self.technical_categories = {
            "backend": ["데이터베이스 설계", "API 설계", "성능 최적화", "보안", "마이크로서비스"],
            "frontend": ["React/Vue 프레임워크", "상태관리", "웹 접근성", "성능 최적화", "모바일 대응"],
            "fullstack": ["시스템 아키텍처", "데이터베이스", "프론트엔드", "백엔드", "DevOps"],
            "devops": ["CI/CD", "컨테이너화", "클라우드", "모니터링", "인프라 코드"],
            "mobile": ["앱 아키텍처", "성능 최적화", "네이티브 기능", "앱스토어 배포", "크로스플랫폼"],
            "data": ["데이터 파이프라인", "머신러닝", "빅데이터 처리", "데이터 시각화", "통계 분석"]
        }
        
        # 경험 수준별 난이도 기준
        self.experience_levels = {
            "junior": {
                "name": "주니어 (0-2년)",
                "focus": "기본 개념, 학습 의지, 문제 해결 태도",
                "depth": "기초적인 이론과 간단한 실습 경험",
                "expectations": "기본 개념 이해와 성장 가능성"
            },
            "mid": {
                "name": "미드레벨 (3-5년)", 
                "focus": "실무 경험, 기술 적용 능력, 트러블슈팅",
                "depth": "실무 프로젝트 경험과 기술적 판단력",
                "expectations": "독립적인 업무 수행과 기술 적용"
            },
            "senior": {
                "name": "시니어 (5년+)",
                "focus": "시스템 설계, 팀 리드, 기술 의사결정", 
                "depth": "아키텍처 설계와 기술 리더십",
                "expectations": "기술 리드와 멘토링 능력"
            }
        }
        
        # 인성면접 전문 카테고리
        self.personality_categories = {
            "leadership": "리더십과 의사결정",
            "communication": "소통과 협업", 
            "problem_solving": "문제 해결과 창의성",
            "growth": "성장과 학습",
            "culture_fit": "조직 적합성과 가치관",
            "stress_management": "스트레스 관리와 적응력"
        }

        # 직무별 특화 질문 패턴
        self.job_specific_patterns = {
            "backend": {
                "core_skills": ["Java", "Spring", "Database", "API", "Performance"],
                "scenarios": ["대용량 트래픽 처리", "데이터베이스 최적화", "시스템 장애 대응"]
            },
            "frontend": {
                "core_skills": ["React", "JavaScript", "CSS", "Performance", "UX"],
                "scenarios": ["사용자 경험 개선", "성능 최적화", "크로스 브라우저 이슈"]
            },
            "fullstack": {
                "core_skills": ["Frontend", "Backend", "Database", "DevOps", "Architecture"],
                "scenarios": ["전체 시스템 설계", "기술 스택 선택", "팀 간 협업"]
            }
        }
    
    async def generate_professional_questions(
        self, 
        interview_type: InterviewType, 
        field: str = "backend",
        experience_level: str = "mid",
        job_title: str = "소프트웨어 엔지니어",
        skills: List[str] = [],
        company_context: str = ""
    ) -> List[InterviewQuestion]:
        """전문적인 면접 질문 생성"""
        
        level_info = self.experience_levels.get(experience_level, self.experience_levels["mid"])
        
        if interview_type == InterviewType.TECHNICAL:
            categories = self.technical_categories.get(field, self.technical_categories["backend"])
            job_patterns = self.job_specific_patterns.get(field, self.job_specific_patterns["backend"])
            
            system_prompt = f"""
            당신은 {job_title} 포지션의 전문 기술 면접관입니다.
            실제 글로벌 IT 기업에서 사용하는 수준의 전문적인 기술면접을 진행해주세요.
            
            지원자 정보:
            - 경력 수준: {level_info['name']}
            - 기술 분야: {field}
            - 핵심 기술: {', '.join(job_patterns['core_skills'])}
            - 보유 기술: {', '.join(skills) if skills else '미명시'}
            - 평가 중점: {level_info['focus']}
            - 예상 지식 수준: {level_info['depth']}
            - 기대 수준: {level_info['expectations']}
            {f"- 회사 특성: {company_context}" if company_context else ""}
            
            다음 전문 도메인에서 각 영역별로 1개씩 총 7개의 고품질 기술면접 질문을 생성해주세요:
            {', '.join(categories)}
            
            질문 작성 원칙:
            1. 실무에서 실제 마주할 수 있는 상황 기반의 시나리오 질문
            2. 단순 암기가 아닌 이해도와 응용능력 평가에 중점
            3. 경력 수준에 적합한 기술적 깊이와 복잡도
            4. 구체적인 기술 스택과 도구에 대한 실무 경험 확인
            5. 문제 해결 과정과 의사결정 논리 파악 가능한 질문
            6. 추가 심화 질문으로 연결 가능한 구조
            
            각 질문마다 다음 정보를 포함해주세요:
            - 핵심 평가 요소 (기술적 이해도, 실무 적용 능력, 문제 해결 능력 등)
            - 예상 답변 키워드 및 핵심 포인트
            - 답변 품질 평가 기준
            - 가능한 후속 질문 예시
            
            JSON 형식으로 응답해주세요:
            {{
                "questions": [
                    {{
                        "question": "구체적이고 실무적인 질문 내용",
                        "category": "기술 카테고리",
                        "difficulty": "{experience_level}",
                        "evaluation_criteria": [
                            "평가 기준 1",
                            "평가 기준 2", 
                            "평가 기준 3"
                        ],
                        "expected_keywords": [
                            "핵심 키워드 1",
                            "핵심 키워드 2",
                            "핵심 키워드 3"
                        ],
                        "follow_up_questions": [
                            "후속 질문 1",
                            "후속 질문 2"
                        ],
                        "real_world_context": "실무 상황 설명"
                    }}
                ]
            }}
            """
        else:  # PERSONALITY
            categories = list(self.personality_categories.values())
            
            system_prompt = f"""
            당신은 {job_title} 포지션의 전문 인사면접관입니다.
            글로벌 IT 기업 수준의 체계적이고 전문적인 인성면접을 진행해주세요.
            
            지원자 정보:
            - 경력 수준: {level_info['name']}
            - 직무: {job_title}
            - 평가 영역: 인성, 조직적합성, 리더십 역량
            - 기대 역할 수준: {level_info['expectations']}
            {f"- 회사 특성: {company_context}" if company_context else ""}
            
            다음 6개 영역에서 각 1-2개씩 총 7개의 고품질 인성면접 질문을 생성해주세요:
            {', '.join(categories)}
            
            질문 작성 원칙:
            1. STAR 기법 (Situation, Task, Action, Result)으로 답변 가능한 구조
            2. 구체적인 상황과 경험을 묻는 행동 중심 질문 (Behavioral Question)
            3. 경력 수준에 맞는 역할과 책임 수준 고려
            4. 지원자의 가치관, 사고방식, 행동 패턴을 파악할 수 있는 질문
            5. 실제 업무 상황에서의 판단력과 대응 능력 평가
            6. 팀워크, 리더십, 문제해결 능력을 종합적으로 평가
            
            각 질문마다 다음 정보를 포함해주세요:
            - STAR 각 단계별 평가 포인트
            - 우수 답변의 특징과 기준
            - 주의깊게 들어야 할 레드 플래그
            - 추가 확인 질문 예시
            
            JSON 형식으로 응답해주세요:
            {{
                "questions": [
                    {{
                        "question": "행동 기반의 구체적인 질문 내용",
                        "category": "인성 카테고리", 
                        "difficulty": "{experience_level}",
                        "star_evaluation": {{
                            "situation": "상황 평가 기준",
                            "task": "과제 평가 기준",
                            "action": "행동 평가 기준",
                            "result": "결과 평가 기준"
                        }},
                        "good_answer_indicators": [
                            "우수 답변 지표 1",
                            "우수 답변 지표 2",
                            "우수 답변 지표 3"
                        ],
                        "red_flags": [
                            "주의 신호 1",
                            "주의 신호 2"
                        ],
                        "follow_up_questions": [
                            "추가 확인 질문 1",
                            "추가 확인 질문 2"
                        ]
                    }}
                ]
            }}
            """
        
        try:
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": "위 요구사항에 따라 전문적인 면접 질문을 생성해주세요."}
            ])
            
            # JSON 파싱
            result = json.loads(response)
            questions = []
            
            for q in result["questions"]:
                questions.append(InterviewQuestion(
                    question=q["question"],
                    category=q["category"],
                    difficulty=q["difficulty"]
                ))
            
            return questions
            
        except json.JSONDecodeError as e:
            logger.error(f"면접 질문 생성 JSON 파싱 실패: {e}")
            # 기본 질문 세트 반환
            return self._get_fallback_questions(interview_type, field, experience_level)
        except Exception as e:
            logger.error(f"면접 질문 생성 실패: {e}")
            raise Exception("전문적인 면접 질문 생성에 실패했습니다")
    
    async def evaluate_answer_professionally(
        self, 
        question: str, 
        answer: str, 
        interview_type: InterviewType,
        field: str = "backend",
        experience_level: str = "mid",
        additional_context: Dict[str, Any] = None
    ) -> InterviewFeedback:
        """전문적인 면접 답변 평가"""
        
        level_info = self.experience_levels.get(experience_level, self.experience_levels["mid"])
        
        if interview_type == InterviewType.TECHNICAL:
            system_prompt = f"""
            당신은 {field} 분야의 전문 기술 면접관입니다.
            글로벌 IT 기업 수준의 엄격하고 체계적인 기술면접 평가를 수행해주세요.
            
            평가 기준:
            - 경력 수준: {level_info['name']} 
            - 기술 분야: {field}
            - 기대 수준: {level_info['expectations']}
            
            세부 평가 항목 (각 20점, 총 100점):
            1. 기본 개념 이해도 (20점): 핵심 개념과 원리에 대한 정확한 이해
            2. 실무 적용 능력 (20점): 이론을 실제 프로젝트에 적용하는 능력  
            3. 문제 해결 논리 (20점): 문제 분석과 해결 과정의 논리성
            4. 기술적 깊이 (20점): 기술에 대한 심층적 이해와 경험
            5. 커뮤니케이션 (20점): 기술적 내용의 명확하고 체계적인 전달
            
            답변 평가 시 고려사항:
            - 구체적인 사례와 경험 제시 여부
            - 기술적 정확성과 최신 동향 반영
            - 대안 접근법에 대한 이해도  
            - 장단점 분석의 균형성
            - 실무 제약사항 고려 여부
            
            JSON 형식으로 전문적이고 상세한 평가를 제공해주세요:
            {{
                "overall_score": 총점수,
                "detailed_scores": {{
                    "concept_understanding": 개념이해점수,
                    "practical_application": 실무적용점수,
                    "problem_solving_logic": 문제해결점수,
                    "technical_depth": 기술깊이점수,
                    "communication_clarity": 커뮤니케이션점수
                }},
                "professional_feedback": "전문가 수준의 상세한 피드백",
                "strengths": [
                    "구체적인 강점 3개"
                ],
                "areas_for_improvement": [
                    "개선이 필요한 영역 3개"
                ],
                "technical_accuracy": "기술적 정확성 평가",
                "experience_level_assessment": "경력 수준 대비 평가",
                "recommended_study_areas": [
                    "추천 학습 영역 3개"
                ],
                "follow_up_questions": [
                    "심화 질문 3개"
                ],
                "industry_perspective": "업계 관점에서의 평가"
            }}
            """
        else:  # PERSONALITY
            system_prompt = f"""
            당신은 전문 인사면접관입니다.
            글로벌 IT 기업 수준의 체계적이고 과학적인 인성면접 평가를 수행해주세요.
            
            평가 기준:
            - 경력 수준: {level_info['name']}
            - 기대 역할: {level_info['expectations']}
            
            세부 평가 항목 (각 20점, 총 100점):
            1. STAR 기법 활용도 (20점): 상황-과제-행동-결과의 체계적 서술
            2. 경험의 구체성 (20점): 실제 경험의 구체성과 진정성
            3. 자기 인식 수준 (20점): 자신의 강점/약점에 대한 객관적 인식
            4. 문제 해결 사고 (20점): 논리적이고 창의적인 문제 해결 과정
            5. 조직 적합성 (20점): 팀워크, 커뮤니케이션, 가치관 일치도
            
            평가 시 중점 확인사항:
            - 답변의 진정성과 일관성
            - 갈등 상황에서의 대응 방식
            - 학습과 성장에 대한 태도
            - 리더십과 팔로워십 균형
            - 스트레스 상황에서의 회복력
            
            JSON 형식으로 전문적이고 심층적인 평가를 제공해주세요:
            {{
                "overall_score": 총점수,
                "detailed_scores": {{
                    "star_method_usage": "STAR기법점수",
                    "experience_specificity": "구체성점수", 
                    "self_awareness": "자기인식점수",
                    "problem_solving_approach": "문제해결점수",
                    "organizational_fit": "조직적합성점수"
                }},
                "behavioral_analysis": "행동 패턴 분석",
                "strengths": [
                    "인성적 강점 3개"
                ],
                "development_areas": [
                    "발전 필요 영역 3개"
                ],
                "leadership_potential": "리더십 잠재력 평가",
                "cultural_fit_assessment": "조직문화 적합성 평가", 
                "stress_resilience": "스트레스 대응 능력",
                "growth_mindset": "성장 마인드셋 평가",
                "recommended_development": [
                    "개발 권장 영역 3개"
                ],
                "interview_follow_up": [
                    "추가 확인 질문 3개"
                ]
            }}
            """
        
        user_prompt = f"""
        면접 질문: {question}
        
        지원자 답변: {answer}
        
        위 답변을 전문가 수준에서 체계적으로 평가해주세요.
        """
        
        try:
            response = await openai_service.generate_completion([
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ])
            
            result = json.loads(response)
            
            return InterviewFeedback(
                score=result["overall_score"],
                feedback=result.get("professional_feedback", result.get("behavioral_analysis", "")),
                strengths=result["strengths"],
                improvements=result.get("areas_for_improvement", result.get("development_areas", [])),
                example_answer="전문가 수준의 답변 예시가 필요한 경우 추가 요청해 주세요."
            )
            
        except json.JSONDecodeError as e:
            logger.error(f"면접 답변 평가 JSON 파싱 실패: {e}")
            return self._get_fallback_feedback()
        except Exception as e:
            logger.error(f"면접 답변 평가 실패: {e}")
            raise Exception("전문적인 면접 답변 평가에 실패했습니다")

    def _get_fallback_questions(self, interview_type: InterviewType, field: str, experience_level: str) -> List[InterviewQuestion]:
        """기본 질문 세트 (API 실패 시 대체용)"""
        
        if interview_type == InterviewType.TECHNICAL:
            questions = [
                InterviewQuestion(
                    question=f"{field} 분야에서 가장 도전적이었던 기술적 문제와 해결 과정을 설명해주세요.",
                    category="문제해결",
                    difficulty=experience_level
                ),
                InterviewQuestion(
                    question="대규모 시스템에서 성능 최적화를 어떻게 접근하시나요?",
                    category="성능최적화", 
                    difficulty=experience_level
                ),
                InterviewQuestion(
                    question="코드 품질을 보장하기 위한 본인만의 방법론이 있다면 공유해주세요.",
                    category="코드품질",
                    difficulty=experience_level
                )
            ]
        else:
            questions = [
                InterviewQuestion(
                    question="팀에서 의견 충돌이 있었을 때 어떻게 해결하셨나요?",
                    category="협업",
                    difficulty=experience_level
                ),
                InterviewQuestion(
                    question="실패한 프로젝트에서 배운 가장 중요한 교훈은 무엇인가요?",
                    category="성장",
                    difficulty=experience_level
                ),
                InterviewQuestion(
                    question="압박이 심한 상황에서 어떻게 우선순위를 정하고 업무를 진행하시나요?",
                    category="스트레스관리",
                    difficulty=experience_level
                )
            ]
        
        return questions

    def _get_fallback_feedback(self) -> InterviewFeedback:
        """기본 피드백 (API 실패 시 대체용)"""
        return InterviewFeedback(
            score=75,
            feedback="답변을 평가했으나 상세한 분석이 어려웠습니다. 더 구체적인 예시나 경험을 포함하여 답변해 주세요.",
            strengths=["기본적인 이해력", "의사소통 능력", "성실한 답변 태도"],
            improvements=["구체적인 사례 제시", "기술적 심화 내용", "실무 경험 연결"],
            example_answer="더 구체적이고 실무 중심적인 답변을 준비해 주세요."
        )

# 글로벌 인스턴스 생성
enhanced_interview_service = EnhancedInterviewService()