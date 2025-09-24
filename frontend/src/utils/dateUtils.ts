/**
 * 날짜 관련 유틸리티 함수들
 */

/**
 * LocalDate 배열 형태를 문자열로 변환
 * @param dateArray - [year, month, day] 형태의 배열 또는 문자열
 * @returns YYYY-MM-DD 형태의 문자열
 */
export function formatEstablishmentDate(dateArray: [number, number, number] | string | null | undefined): string {
  if (!dateArray) return '';

  // 이미 문자열인 경우 그대로 반환
  if (typeof dateArray === 'string') {
    return dateArray;
  }

  // 배열인 경우 문자열로 변환
  if (Array.isArray(dateArray) && dateArray.length === 3) {
    const [year, month, day] = dateArray;
    // month와 day를 2자리로 패딩
    const formattedMonth = month.toString().padStart(2, '0');
    const formattedDay = day.toString().padStart(2, '0');
    return `${year}-${formattedMonth}-${formattedDay}`;
  }

  return '';
}

/**
 * 날짜 문자열을 사용자 친화적인 형태로 변환
 * @param dateString - YYYY-MM-DD 형태의 문자열 또는 [year, month, day] 배열
 * @returns YYYY년 MM월 DD일 형태의 문자열
 */
export function formatDisplayDate(dateString: string | [number, number, number] | null | undefined): string {
  if (!dateString) return '설립일 없음';

  let formattedDate = '';

  if (Array.isArray(dateString)) {
    formattedDate = formatEstablishmentDate(dateString);
  } else {
    formattedDate = dateString;
  }

  if (!formattedDate) return '설립일 없음';

  try {
    const date = new Date(formattedDate);
    if (isNaN(date.getTime())) return '설립일 없음';

    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();

    return `${year}년 ${month}월 ${day}일`;
  } catch (error) {
    return '설립일 없음';
  }
}