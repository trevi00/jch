#!/usr/bin/env python3
"""
AI 면접 서비스 - 면접 질문 생성 및 답변 평가
"""

import logging
from typing import List, Dict, Any, Optional
import json
from datetime import datetime

from services.openai_service import openai_service
from config import settings

logger = logging.getLogger(__name__)

class InterviewService:
    """AI 면접 서비스 클래스"""
    
    def __init__(self):
        """서비스 초기화"""
        self.client = openai_service.client
        self.technical_categories = [
            "프로그래밍 기초", "자료구조/알고리즘", "데이터베이스",
            "웹 개발", "프레임워크", "시스템 설계", "클라우드",
            "DevOps", "보안", "테스팅"
        ]
        self.behavioral_categories = [
            "리더십", "팀워크", "문제해결", "의사소통",
            "적응력", "창의성", "시간관리", "갈등해결"
        ]
    
    async def generate_questions(
        self,
        position: str,
        interview_type: str = "technical",
        difficulty: str = "intermediate",
        count: int = 5
    ) -> Dict[str, Any]:
        """면접 질문 생성"""
        try:
            logger.info(f"면접 질문 생성 - 직무: {position}, 유형: {interview_type}, 난이도: {difficulty}")
            
            if interview_type == "technical":
                prompt = self._create_technical_prompt(position, difficulty, count)
            else:
                prompt = self._create_behavioral_prompt(position, count)
            
            response = self.client.chat.completions.create(
                model=settings.LLM_MODEL,
                messages=[{"role": "user", "content": prompt}],
                temperature=0.7,
                max_tokens=1500
            )
            
            questions_text = response.choices[0].message.content
            questions = self._parse_questions(questions_text, interview_type, difficulty)
            
            logger.info(f"면접 질문 {len(questions)}개 생성 완료")
            
            return {
                "success": True,
                "questions": questions,
                "position": position,
                "interview_type": interview_type,
                "difficulty": difficulty,
                "generated_at": datetime.now().isoformat()
            }
            
        except Exception as e:
            logger.error(f"면접 질문 생성 실패: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    async def evaluate_answer(
        self,
        question: str,
        answer: str,
        position: str,
        interview_type: str = "technical"
    ) -> Dict[str, Any]:
        """면접 답변 평가"""
        try:
            logger.info(f"면접 답변 평가 - 직무: {position}")
            
            prompt = f"""
당신은 전문적인 면접관입니다. 다음 조건에 따라 답변을 평가해주세요:

직무: {position}
면접 유형: {interview_type}
질문: {question}
답변: {answer}

다음 기준으로 평가하고 JSON 형태로 응답해주세요:

1. 점수 (0-100점)
2. 강점 (좋았던 부분들)
3. 개선점 (부족했던 부분들) 
4. 구체적인 피드백
5. 모범 답변 예시

형식:
{{
  "score": 85,
  "strengths": ["명확한 설명", "적절한 예시"],
  "improvements": ["더 구체적인 경험 필요"],
  "feedback": "전반적으로 좋은 답변입니다...",
  "sample_answer": "모범 답변 예시..."
}}
"""

            response = self.client.chat.completions.create(
                model=settings.LLM_MODEL,
                messages=[{"role": "user", "content": prompt}],
                temperature=0.3,
                max_tokens=1000
            )
            
            evaluation_text = response.choices[0].message.content
            evaluation = self._parse_evaluation(evaluation_text)
            
            logger.info(f"면접 답변 평가 완료 - 점수: {evaluation.get('score', 0)}")
            
            return {
                "success": True,
                "evaluation": evaluation,
                "question": question,
                "answer": answer,
                "evaluated_at": datetime.now().isoformat()
            }
            
        except Exception as e:
            logger.error(f"면접 답변 평가 실패: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    async def complete_interview(
        self,
        session_id: str,
        answers: List[Dict[str, Any]]
    ) -> Dict[str, Any]:
        """면접 세션 종합 평가"""
        try:
            logger.info(f"면접 세션 완료 - ID: {session_id}, 답변 수: {len(answers)}")
            
            # 각 답변 평가
            evaluations = []
            total_score = 0
            
            for answer_data in answers:
                evaluation = await self.evaluate_answer(
                    answer_data.get("question", ""),
                    answer_data.get("answer", ""),
                    answer_data.get("position", "개발자")
                )
                if evaluation["success"]:
                    evaluations.append(evaluation["evaluation"])
                    total_score += evaluation["evaluation"].get("score", 0)
            
            average_score = total_score / len(evaluations) if evaluations else 0
            
            # 종합 피드백 생성
            overall_feedback = self._generate_overall_feedback(evaluations, average_score)
            
            return {
                "success": True,
                "session_id": session_id,
                "total_questions": len(answers),
                "average_score": round(average_score, 1),
                "grade": self._get_grade(average_score),
                "evaluations": evaluations,
                "overall_feedback": overall_feedback,
                "completed_at": datetime.now().isoformat()
            }
            
        except Exception as e:
            logger.error(f"면접 세션 완료 처리 실패: {e}")
            return {
                "success": False,
                "error": str(e)
            }
    
    def _create_technical_prompt(self, position: str, difficulty: str, count: int) -> str:
        """기술 면접 질문 생성 프롬프트"""
        return f"""
{position} 직무를 위한 {difficulty} 수준의 기술 면접 질문을 {count}개 생성해주세요.

다음 형식으로 응답해주세요:
1. [카테고리] 질문 내용
2. [카테고리] 질문 내용
...

요구사항:
- 실무에서 정말 중요한 질문들
- {difficulty} 수준에 맞는 난이도
- 구체적이고 실용적인 질문
- 다양한 기술 분야 포함

카테고리: {', '.join(self.technical_categories)}
"""

    def _create_behavioral_prompt(self, position: str, count: int) -> str:
        """인성 면접 질문 생성 프롬프트"""
        return f"""
{position} 직무를 위한 인성/행동 면접 질문을 {count}개 생성해주세요.

다음 형식으로 응답해주세요:
1. [카테고리] 질문 내용
2. [카테고리] 질문 내용
...

요구사항:
- 지원자의 소프트 스킬을 평가할 수 있는 질문
- STAR 방법으로 답변 가능한 질문
- 실제 업무 상황과 연관된 질문

카테고리: {', '.join(self.behavioral_categories)}
"""
    
    def _parse_questions(self, questions_text: str, interview_type: str, difficulty: str) -> List[Dict[str, Any]]:
        """질문 텍스트를 파싱하여 구조화"""
        questions = []
        lines = questions_text.strip().split('\n')
        
        for i, line in enumerate(lines, 1):
            if line.strip() and not line.strip().startswith('#'):
                # 카테고리와 질문 분리
                if '[' in line and ']' in line:
                    category_end = line.find(']')
                    category = line[line.find('[')+1:category_end]
                    question = line[category_end+1:].strip()
                else:
                    category = "일반"
                    question = line.strip()
                    # 번호 제거
                    if question.split('.', 1):
                        try:
                            int(question.split('.', 1)[0])
                            question = question.split('.', 1)[1].strip()
                        except:
                            pass
                
                if question:
                    questions.append({
                        "id": i,
                        "question": question,
                        "category": category,
                        "difficulty": difficulty,
                        "interview_type": interview_type
                    })
        
        return questions
    
    def _parse_evaluation(self, evaluation_text: str) -> Dict[str, Any]:
        """평가 텍스트를 JSON으로 파싱"""
        try:
            # JSON 부분 찾기
            start = evaluation_text.find('{')
            end = evaluation_text.rfind('}') + 1
            
            if start != -1 and end > start:
                json_text = evaluation_text[start:end]
                return json.loads(json_text)
            else:
                # JSON이 없으면 기본 구조로 파싱
                return {
                    "score": 70,
                    "strengths": ["답변을 제공함"],
                    "improvements": ["더 구체적인 설명 필요"],
                    "feedback": evaluation_text,
                    "sample_answer": "추가적인 예시와 경험을 포함하여 답변해보세요."
                }
                
        except json.JSONDecodeError:
            return {
                "score": 70,
                "strengths": ["기본적인 답변 제공"],
                "improvements": ["더 상세한 설명 필요"],
                "feedback": evaluation_text,
                "sample_answer": ""
            }
    
    def _generate_overall_feedback(self, evaluations: List[Dict], average_score: float) -> str:
        """종합 피드백 생성"""
        if not evaluations:
            return "평가할 수 있는 답변이 없습니다."
        
        # 강점과 개선점 수집
        all_strengths = []
        all_improvements = []
        
        for eval_data in evaluations:
            all_strengths.extend(eval_data.get("strengths", []))
            all_improvements.extend(eval_data.get("improvements", []))
        
        # 중복 제거
        unique_strengths = list(set(all_strengths))
        unique_improvements = list(set(all_improvements))
        
        feedback = f"""
전체 면접 평가 결과:

평균 점수: {average_score:.1f}점 ({self._get_grade(average_score)})

주요 강점:
{chr(10).join(f"• {strength}" for strength in unique_strengths[:3])}

개선이 필요한 부분:
{chr(10).join(f"• {improvement}" for improvement in unique_improvements[:3])}

종합 의견:
"""
        
        if average_score >= 90:
            feedback += "매우 우수한 면접 성과입니다. 모든 질문에 대해 체계적이고 전문적인 답변을 하셨습니다."
        elif average_score >= 80:
            feedback += "좋은 면접 성과입니다. 대부분의 질문에 적절한 답변을 하셨으나 일부 개선점이 있습니다."
        elif average_score >= 70:
            feedback += "보통 수준의 면접 성과입니다. 기본적인 답변은 하셨으나 더 구체적인 준비가 필요합니다."
        else:
            feedback += "면접 준비가 더 필요합니다. 각 질문에 대해 체계적으로 준비하고 연습해보세요."
        
        return feedback
    
    def _get_grade(self, score: float) -> str:
        """점수를 등급으로 변환"""
        if score >= 90:
            return "A"
        elif score >= 80:
            return "B"
        elif score >= 70:
            return "C"
        elif score >= 60:
            return "D"
        else:
            return "F"

# 전역 인스턴스
interview_service = InterviewService()