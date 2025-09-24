import os
from typing import Dict, Any
from pathlib import Path
import logging
from datetime import datetime
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch, mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY

logger = logging.getLogger(__name__)

class PDFService:
    def __init__(self):
        self.output_dir = Path("generated_resumes")
        self.output_dir.mkdir(exist_ok=True)
        
        # 폰트 등록 (한글 지원)
        self._register_fonts()
        
    def _register_fonts(self):
        """한글 폰트 등록"""
        try:
            # Windows 기본 폰트 사용
            font_path = "C:/Windows/Fonts/malgun.ttf"
            if os.path.exists(font_path):
                pdfmetrics.registerFont(TTFont('MalgunGothic', font_path))
                logger.info("말굼고딕 폰트 등록 성공")
            else:
                # 대체 폰트 시도
                font_paths = [
                    "C:/Windows/Fonts/gulim.ttc",
                    "C:/Windows/Fonts/batang.ttc"
                ]
                for font_path in font_paths:
                    if os.path.exists(font_path):
                        pdfmetrics.registerFont(TTFont('MalgunGothic', font_path))
                        logger.info(f"대체 폰트 등록 성공: {font_path}")
                        break
                else:
                    logger.warning("한글 폰트를 찾을 수 없습니다. 기본 폰트를 사용합니다.")
        except Exception as e:
            logger.error(f"폰트 등록 실패: {e}")
    
    def _create_styles(self):
        """PDF 스타일 정의"""
        styles = getSampleStyleSheet()
        
        # 제목 스타일
        title_style = ParagraphStyle(
            'CustomTitle',
            parent=styles['Title'],
            fontName='MalgunGothic',
            fontSize=20,
            alignment=TA_CENTER,
            spaceAfter=20,
            textColor=colors.HexColor('#2E5090')
        )
        
        # 섹션 제목 스타일
        section_title_style = ParagraphStyle(
            'SectionTitle',
            parent=styles['Heading1'],
            fontName='MalgunGothic',
            fontSize=16,
            alignment=TA_LEFT,
            spaceAfter=12,
            spaceBefore=20,
            textColor=colors.HexColor('#1F4788'),
            borderWidth=0,
            borderPadding=0,
        )
        
        # 본문 스타일
        body_style = ParagraphStyle(
            'CustomBody',
            parent=styles['Normal'],
            fontName='MalgunGothic',
            fontSize=11,
            alignment=TA_JUSTIFY,
            spaceAfter=10,
            lineSpacing=1.3,
            leftIndent=10,
            rightIndent=10
        )
        
        # 메타 정보 스타일
        meta_style = ParagraphStyle(
            'MetaInfo',
            parent=styles['Normal'],
            fontName='MalgunGothic',
            fontSize=9,
            alignment=TA_CENTER,
            textColor=colors.grey,
            spaceAfter=5
        )
        
        return {
            'title': title_style,
            'section_title': section_title_style,
            'body': body_style,
            'meta': meta_style
        }
    
    def generate_cover_letter_pdf(
        self,
        company_name: str,
        position: str,
        user_name: str,
        sections: Dict[str, str],
        user_info: Dict[str, Any] = {}
    ) -> str:
        """자기소개서 PDF 생성"""
        
        try:
            # 파일명 생성
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"자기소개서_{company_name}_{position}_{timestamp}.pdf"
            file_path = self.output_dir / filename
            
            # PDF 문서 생성
            doc = SimpleDocTemplate(
                str(file_path),
                pagesize=A4,
                rightMargin=20*mm,
                leftMargin=20*mm,
                topMargin=25*mm,
                bottomMargin=25*mm
            )
            
            # 스타일 가져오기
            styles = self._create_styles()
            
            # 콘텐츠 구성
            story = []
            
            # 제목
            title = f"{company_name} {position} 지원서"
            story.append(Paragraph(title, styles['title']))
            story.append(Spacer(1, 15))
            
            # 지원자 정보
            if user_info:
                info_data = [
                    ['지원자명', user_name],
                    ['학력', user_info.get('education', '정보 없음')],
                    ['전공', user_info.get('major', '정보 없음')],
                    ['기술스택', ', '.join(user_info.get('skills', []))],
                    ['경력', user_info.get('experience', '신입')]
                ]
                
                info_table = Table(info_data, colWidths=[40*mm, 120*mm])
                info_table.setStyle(TableStyle([
                    ('FONTNAME', (0, 0), (-1, -1), 'MalgunGothic'),
                    ('FONTSIZE', (0, 0), (-1, -1), 10),
                    ('ALIGN', (0, 0), (0, -1), 'RIGHT'),
                    ('ALIGN', (1, 0), (1, -1), 'LEFT'),
                    ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
                    ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
                    ('ROWBACKGROUNDS', (0, 0), (-1, -1), [colors.white, colors.HexColor('#f8f9fa')])
                ]))
                
                story.append(info_table)
                story.append(Spacer(1, 20))
            
            # 섹션별 내용
            section_titles = {
                '지원동기': '지원동기 및 입사 후 포부',
                '성장과정': '성장과정',
                '나의 장점': '성격의 장단점 및 특기사항',
                '커뮤니케이션': '커뮤니케이션 및 대인관계'
            }
            
            for section_key, content in sections.items():
                if not content.strip():
                    continue
                    
                # 섹션 제목
                section_title = section_titles.get(section_key, section_key)
                story.append(Paragraph(f"◆ {section_title}", styles['section_title']))
                
                # 구분선
                line_table = Table([[''] * 10], colWidths=[16*mm] * 10, rowHeights=[1])
                line_table.setStyle(TableStyle([
                    ('LINEABOVE', (0, 0), (-1, -1), 2, colors.HexColor('#1F4788')),
                ]))
                story.append(line_table)
                story.append(Spacer(1, 8))
                
                # 내용
                story.append(Paragraph(content, styles['body']))
                story.append(Spacer(1, 15))
            
            # 푸터
            story.append(Spacer(1, 20))
            footer_text = f"생성일시: {datetime.now().strftime('%Y년 %m월 %d일 %H:%M')} | 잡았다 AI 자기소개서 생성 서비스"
            story.append(Paragraph(footer_text, styles['meta']))
            
            # PDF 빌드
            doc.build(story)
            
            logger.info(f"PDF 생성 완료: {file_path}")
            return str(file_path)
            
        except Exception as e:
            logger.error(f"PDF 생성 실패: {e}")
            raise Exception(f"PDF 생성 중 오류가 발생했습니다: {str(e)}")
    
    def get_generated_files(self) -> list:
        """생성된 PDF 파일 목록 반환"""
        try:
            pdf_files = list(self.output_dir.glob("*.pdf"))
            return [
                {
                    'filename': file.name,
                    'path': str(file),
                    'created': datetime.fromtimestamp(file.stat().st_mtime).strftime('%Y-%m-%d %H:%M:%S'),
                    'size': f"{file.stat().st_size / 1024:.1f} KB"
                }
                for file in sorted(pdf_files, key=lambda x: x.stat().st_mtime, reverse=True)
            ]
        except Exception as e:
            logger.error(f"파일 목록 조회 실패: {e}")
            return []
    
    def delete_file(self, filename: str) -> bool:
        """생성된 PDF 파일 삭제"""
        try:
            file_path = self.output_dir / filename
            if file_path.exists() and file_path.suffix == '.pdf':
                file_path.unlink()
                logger.info(f"파일 삭제 완료: {filename}")
                return True
            return False
        except Exception as e:
            logger.error(f"파일 삭제 실패: {e}")
            return False

# 싱글톤 인스턴스
pdf_service = PDFService()