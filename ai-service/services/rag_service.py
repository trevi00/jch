import os
import json
from typing import List, Dict, Any
from sentence_transformers import SentenceTransformer
import numpy as np
import faiss
from pathlib import Path
import logging

logger = logging.getLogger(__name__)

class RAGService:
    def __init__(self):
        self.model = SentenceTransformer('sentence-transformers/all-MiniLM-L6-v2')
        self.index = None
        self.documents = []
        self.metadata = []
        self.vector_store_path = "data/vector_store"
        self.dataset_path = "resume_dataset"
        
        # 벡터 저장소 디렉토리 생성
        Path(self.vector_store_path).mkdir(parents=True, exist_ok=True)
        
        # 초기 로드
        self._load_or_create_vector_store()
    
    def _load_resume_dataset(self) -> List[Dict[str, str]]:
        """자소서 데이터셋 로드"""
        documents = []
        dataset_files = {
            "motivation": "지원 동기.txt",
            "growth": "성장과정.txt", 
            "strengths": "나의 장점.txt",
            "communication": "커뮤니케이션.txt",
            "company_analysis": "기업 분석.txt",
            "portfolio_analysis": "포트폴리오 분석.txt"
        }
        
        for section, filename in dataset_files.items():
            file_path = Path(self.dataset_path) / filename
            if file_path.exists():
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()
                        
                    # 텍스트를 청크로 분할
                    chunks = self._split_text(content, chunk_size=500, overlap=50)
                    
                    for i, chunk in enumerate(chunks):
                        documents.append({
                            "text": chunk,
                            "section": section,
                            "source": filename,
                            "chunk_id": i,
                            "metadata": {
                                "section": section,
                                "source": filename,
                                "chunk_id": i
                            }
                        })
                        
                except Exception as e:
                    logger.error(f"파일 읽기 오류 {filename}: {e}")
                    
        return documents
    
    def _split_text(self, text: str, chunk_size: int = 500, overlap: int = 50) -> List[str]:
        """텍스트를 청크로 분할"""
        chunks = []
        start = 0
        
        while start < len(text):
            end = start + chunk_size
            
            # 문장 경계에서 자르기
            if end < len(text):
                # 가장 가까운 문장 끝 찾기
                for punct in ['.', '!', '?', '\n\n']:
                    punct_pos = text.rfind(punct, start, end)
                    if punct_pos > start:
                        end = punct_pos + 1
                        break
            
            chunk = text[start:end].strip()
            if chunk:
                chunks.append(chunk)
            
            start = end - overlap if end < len(text) else end
            
        return chunks
    
    def _create_embeddings(self, texts: List[str]) -> np.ndarray:
        """텍스트 임베딩 생성"""
        embeddings = self.model.encode(texts, show_progress_bar=True)
        return embeddings.astype('float32')
    
    def _create_vector_store(self):
        """벡터 저장소 생성"""
        logger.info("자소서 데이터셋 벡터화 시작...")
        
        # 데이터셋 로드
        documents = self._load_resume_dataset()
        texts = [doc["text"] for doc in documents]
        
        if not texts:
            logger.error("데이터셋이 비어있습니다")
            return
        
        # 임베딩 생성
        embeddings = self._create_embeddings(texts)
        
        # FAISS 인덱스 생성
        dimension = embeddings.shape[1]
        self.index = faiss.IndexFlatIP(dimension)  # Inner Product (코사인 유사도)
        
        # 정규화 (코사인 유사도를 위해)
        faiss.normalize_L2(embeddings)
        self.index.add(embeddings)
        
        # 문서와 메타데이터 저장
        self.documents = texts
        self.metadata = [doc["metadata"] for doc in documents]
        
        # 디스크에 저장
        self._save_vector_store()
        
        logger.info(f"벡터 저장소 생성 완료: {len(texts)}개 문서")
    
    def _save_vector_store(self):
        """벡터 저장소를 디스크에 저장"""
        try:
            # FAISS 인덱스 저장
            faiss.write_index(self.index, f"{self.vector_store_path}/index.faiss")
            
            # 메타데이터 저장
            with open(f"{self.vector_store_path}/documents.json", 'w', encoding='utf-8') as f:
                json.dump({
                    "documents": self.documents,
                    "metadata": self.metadata
                }, f, ensure_ascii=False, indent=2)
                
            logger.info("벡터 저장소 저장 완료")
            
        except Exception as e:
            logger.error(f"벡터 저장소 저장 실패: {e}")
    
    def _load_vector_store(self):
        """디스크에서 벡터 저장소 로드"""
        try:
            index_path = f"{self.vector_store_path}/index.faiss"
            meta_path = f"{self.vector_store_path}/documents.json"
            
            if os.path.exists(index_path) and os.path.exists(meta_path):
                # FAISS 인덱스 로드
                self.index = faiss.read_index(index_path)
                
                # 메타데이터 로드
                with open(meta_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    self.documents = data["documents"]
                    self.metadata = data["metadata"]
                
                logger.info(f"벡터 저장소 로드 완료: {len(self.documents)}개 문서")
                return True
            
        except Exception as e:
            logger.error(f"벡터 저장소 로드 실패: {e}")
            
        return False
    
    def _load_or_create_vector_store(self):
        """벡터 저장소 로드하거나 생성"""
        if not self._load_vector_store():
            self._create_vector_store()
    
    def search_similar_documents(
        self, 
        query: str, 
        section: str = None, 
        top_k: int = 5
    ) -> List[Dict[str, Any]]:
        """유사한 문서 검색"""
        if self.index is None or not self.documents:
            logger.error("벡터 저장소가 초기화되지 않았습니다")
            return []
        
        try:
            # 쿼리 임베딩
            query_embedding = self.model.encode([query]).astype('float32')
            faiss.normalize_L2(query_embedding)
            
            # 검색
            scores, indices = self.index.search(query_embedding, min(top_k * 2, len(self.documents)))
            
            results = []
            for score, idx in zip(scores[0], indices[0]):
                if idx == -1:  # FAISS에서 -1은 유효하지 않은 인덱스
                    continue
                    
                metadata = self.metadata[idx]
                
                # 섹션 필터링
                if section and metadata.get("section") != section:
                    continue
                
                results.append({
                    "text": self.documents[idx],
                    "score": float(score),
                    "metadata": metadata
                })
                
                if len(results) >= top_k:
                    break
            
            return results
            
        except Exception as e:
            logger.error(f"문서 검색 실패: {e}")
            return []
    
    def get_context_for_section(self, section: str, query: str = "", top_k: int = 3) -> str:
        """특정 섹션의 컨텍스트 가져오기"""
        if query:
            results = self.search_similar_documents(query, section, top_k)
        else:
            # 섹션의 모든 문서 가져오기
            results = []
            for i, metadata in enumerate(self.metadata):
                if metadata.get("section") == section:
                    results.append({
                        "text": self.documents[i],
                        "score": 1.0,
                        "metadata": metadata
                    })
            results = results[:top_k]
        
        context_texts = [result["text"] for result in results]
        return "\n\n".join(context_texts)

# 싱글톤 인스턴스
rag_service = RAGService()