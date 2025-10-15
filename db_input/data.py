# 웹페이지 크롤링

import requests
from bs4 import BeautifulSoup

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36'
}

def crawled_url(url):
    response = requests.get(url, headers=headers)

    soup = BeautifulSoup(response.text, 'html.parser')

    text = soup.get_text()

    with open('crawled_url.txt', 'w', encoding='utf-8') as f:
        f.write(text)
    
    return text



# 기본적인 엑셀 파일 읽기 함수 (XLSX 파일은 openpyxl이나 pandas, XLS는 xlrd 등으로 읽음)

import requests
import pandas as pd
from io import BytesIO

def crawled_excel(url = 'https://www.uos.ac.kr/common/board-download.do?listId=CA2&seq=100&fSeq=1'):
    response = requests.get(url, headers=headers)
    response.raise_for_status()

    excel_data = BytesIO(response.content)
    df = pd.read_excel(excel_data)

    df.to_csv('crawled_excel.txt', sep='\t', index=False, encoding='utf-8') #to_csv로 바로 텍스트 파일 형태로 저장 가능. 구분자(sep)는 자유롭게 지정
    
    return df



# 기본적인 한컴 파일 읽기 함수

import requests
import olefile
from io import BytesIO

def crawled_hwp(url = 'https://blog.kakaocdn.net/dna/ISFm3/btsHA3oerTg/AAAAAAAAAAAAAAAAAAAAAAhkUIkWvmoNeaEakmx5wAtbVqd3CmelBIZPbwAlO-Wk/%EB%B3%B4%EA%B3%A0%EC%84%9C%20%EC%96%91%EC%8B%9D.hwp?credential=yqXZFxpELC7KVnFOS48ylbz2pIh7yKj8&expires=1761922799&allow_ip=&allow_referer=&signature=ccqm0PrWZCp5%2FCdOkrDnKY3X1io%3D&attach=1&knm=tfile.hwp'):
    response = requests.get(url, headers=headers)
    response.raise_for_status()

    file_stream = BytesIO(response.content)
    hwp = olefile.OleFileIO(file_stream)

    encoded_txt = hwp.openstream('PrvText').read()
    text = encoded_txt.decode('utf-16', errors='ignore')

    with open('crawled_hwp.txt', 'w', encoding='utf-8') as f:
        f.write(text)

    return text



# 학교 API 활용

import requests

api_key = '202509252ERW65625'

# 수업계획서조회
def get_course_info(api_key, year, term, subject_no, dvcl_no):
    url = 'https://wise.uos.ac.kr/COM/ApiCoursePlan/list.do'
    params = {
        'apiKey': api_key,
        'year': year,
        'term': term,              # 1학기=10, 2학기=20
        'subjectNo': subject_no,
        'dvclNo': dvcl_no          # 01분반=01, 02분반=02 등
    }

    response = requests.post(url, data=params)
    response.raise_for_status()
    data = response.json()
    info_list = data.get('INFO', [])

    result_lines = []
    for week_info in info_list:
        lines = [
            "-" * 30,
            f"주차: {week_info.get('WEEK', '')}",
            f"학년도: {week_info.get('YEAR', '')}",
            f"학기: {week_info.get('TERM', '')}",
            f"교과목번호: {week_info.get('SUBJECT_NO', '')}",
            f"교과목명: {week_info.get('SUBJECT_NM', '')}",
            f"분반번호: {week_info.get('DVCL_NO', '')}",
            f"학년: {week_info.get('SHYR', '')}",
            f"학부(과): {week_info.get('SUB_DEPT', '')}",
            f"전화번호: {week_info.get('TEL', '')}",
            f"강의실습구분: {week_info.get('CLASS_PRAC_NM', '')}",
            f"평가방법: {week_info.get('EVAL_METHOD_NM', '')}",
            f"수업구분: {week_info.get('LSN_DIV_NM', '')}",
            f"수업내용: {week_info.get('THEMA_CN', '')}",
            f"교재내용: {week_info.get('TCHMTR_CN', '')}",
            f"과제내용: {week_info.get('ASMT_CN', '')}",
            f"수업방법내용: {week_info.get('LESN_MTH_CN', '')}",
            f"강의유형: {week_info.get('CLASS_TYPE', '')}",
            f"수업목표: {week_info.get('LCTRE_GOAL_CN', '')}",
            f"핵심역량: {week_info.get('CORE_COMP', '')}",
            f"담당교수명: {week_info.get('PROF_KOR_NM', '')}",
            f"이메일: {week_info.get('MAIL', '')}"
        ]
        result_lines.append("\n".join(lines))

    result_text = "\n".join(result_lines)
    with open("course_info.txt", "w", encoding="utf-8") as f:
        f.write(result_text)
    return result_text

# 건물강의실조회
def get_building_room_info(apiKey):
    url = "https://wise.uos.ac.kr/COM/ApiBldg/list.do"
    params = {
        "apiKey": apiKey
    }

    response = requests.post(url, data=params)
    response.raise_for_status()

    data = response.json()
    info_list = data.get('INFO', [])

    result_lines = []
    for item in info_list:
        lines = [
            f"건물이름: {item.get('BUILDING_NM', '').strip()}",
            f"건물코드: {item.get('BUILDING', '').strip()}",
            f"분반번호(공간명): {item.get('SPCE_NM', '').strip()}",
            f"강의실명: {item.get('ROOM_NM', '').strip()}",
            f"강의실코드: {item.get('ROOM_CD', '').strip()}",
            "-" * 40
        ]
        result_lines.append("\n".join(lines))

    result_text = "\n".join(result_lines)
    with open("building_room_info.txt", "w", encoding="utf-8") as f:
        f.write(result_text)

    return result_text

# 부서조회
def search_department(api_key, dept_name='', open_yn=''):
    url = 'https://wise.uos.ac.kr/COM/ApiDept/list.do'
    params = {
        'apiKey': api_key,
        'deptNm': dept_name,        # 검색할 부서명, 필요에 따라 변경, 필수 입력값 아님
        'openYn': open_yn           # 현재 운영 여부(Y 또는 N) 필수 입력값 아님
    }

    response = requests.post(url, data=params)
    response.raise_for_status()
    data = response.json()

    result_lines = []

    # 소속구분: deptDivList
    dept_div_list = data.get('deptDivList', [])
    result_lines.append('--- 소속구분 ---')
    for dept in dept_div_list:
        lines = [
            f"부서명: {dept.get('DEPT_NM', '')}",
            f"부서코드: {dept.get('DEPT_CD', '')}",
            f"상위부서명: {dept.get('UP_DEPT_NM', '')}",
            f"부서구분: {dept.get('DEPT_DIV', '')}",
            '---------------'
        ]
        result_lines.append("\n".join(lines))

    # 대학: deptList
    dept_list = data.get('deptList', [])
    result_lines.append('--- 대학 ---')
    for dept in dept_list:
        lines = [
            f"부서명: {dept.get('DEPT_NM', '')}",
            f"부서코드: {dept.get('DEPT_CD', '')}",
            f"상위부서명: {dept.get('UP_DEPT_NM', '')}",
            f"부서구분: {dept.get('DEPT_DIV', '')}",
            '---------------'
        ]
        result_lines.append("\n".join(lines))

    # 학부(과): subDeptList
    sub_dept_list = data.get('subDeptList', [])
    result_lines.append('--- 학부(과) ---')
    for dept in sub_dept_list:
        lines = [
            f"부서명: {dept.get('DEPT_NM', '')}",
            f"부서코드: {dept.get('DEPT_CD', '')}",
            f"상위부서명: {dept.get('UP_DEPT_NM', '')}",
            f"부서구분: {dept.get('DEPT_DIV', '')}",
            f"대학명: {dept.get('COLG_NM', '')}",
            '---------------'
        ]
        result_lines.append("\n".join(lines))

    result_text = "\n".join(result_lines)
    with open("department_info.txt", "w", encoding="utf-8") as f:
        f.write(result_text)

    return result_text

# 교과목검색
def search_subject(api_key, year, term, subject_nm, subject_no='', dvcl_no='', subject_div='', dept_cd='', prof_nm='', univ_gdhl_dept_cd=''):
    url = 'https://wise.uos.ac.kr/COM/ApiSubject/list.do'

    params = {
        'apiKey': api_key,
        'year': year,
        'term': term,                   # 1학기=10, 2학기=20
        'subjectNo': subject_no,                # 선택사항
        'subjectNm': subject_nm,        # 과목명(예: 컴퓨터)
        'dvclNo': dvcl_no,                      # 선택사항 / 01분반=01, 02분반=02 등
        'subjectDiv': subject_div,              # 선택사항 (01 : 교양선택, 02 : 교양필수, 03 : 전공필수, 04 : 전공선택, 05 : 일반선택, 06 : ROTC, 07 : 교직, 08 : 교환학점, 09 : 선수, 10 : 기초선택, 11 : 공통선택)
        'deptCd': dept_cd,                      # 선택사항 / 부서코드 -> 부서검색에서 확인 가능
        'profNm': prof_nm,                      # 선택사항 / 교수명(예: 홍길동)
        'univGdhlDeptCd': univ_gdhl_dept_cd     # 선택사항 / 대학(1) 또는 대학원(2)
    }

    response = requests.post(url, data=params)
    response.raise_for_status()

    data = response.json()
    subject_list = data.get('subjectList', [])

    if not subject_list:
        print("교과목 조회 결과가 없습니다.")
        return

    result_lines = []
    for subject in subject_list:
        lines = [
            f"교과목번호: {subject.get('SUBJECT_NO', '')}",
            f"교과목명: {subject.get('SUBJECT_NM', '')}",
            f"분반번호: {subject.get('DVCL_NO', '')}",
            f"교과구분: {subject.get('SUBJECT_DIV', '')}",
            f"개설학부(과): {subject.get('DEPT', '')}",
            f"학점: {subject.get('PNT', '')}",
            f"대표교수명: {subject.get('PROF_NM', '')}",
            "-" * 30
        ]
        result_lines.append("\n".join(lines))

    result_text = "\n".join(result_lines)
    with open("subject_info.txt", "w", encoding="utf-8") as f:
        f.write(result_text)

    return result_text

# 시간표조회 => 사용안할 듯
def get_timetable(api_key, year, term):
    url = 'https://wise.uos.ac.kr/COM/ApiTimeTable/list.do'
    params = {
        'apiKey': api_key,
        'year': year,
        'term': term  # 1학기=10, 2학기=20
    }

    response = requests.post(url, data=params)
    response.raise_for_status()
    data = response.json()
    timetable_list = data.get('INFO', [])

    if not timetable_list:
        return "시간표 조회 결과가 없습니다."

    result_lines = []
    for item in timetable_list:
        lines = [
            f"학년도: {item.get('YEAR', '')}",
            f"학기: {item.get('TERM', '')}",
            f"교과목번호: {item.get('SUBJECT_NO', '')}",
            f"교과목명: {item.get('SUBJECT_NM', '')}",
            f"분반번호: {item.get('DVCL_NO', '')}",
            f"학년: {item.get('SHYR', '')}",
            f"교과구분코드: {item.get('SUBJECT_DIV2', '')}",
            f"교과구분: {item.get('SUBJECT_DIV', '')}",
            f"학부(과): {item.get('SUB_DEPT', '')}",
            f"주야: {item.get('DAY_NIGHT_NM', '')}",
            f"학점: {item.get('CREDIT', '')}",
            f"강의시간 및 강의실: {item.get('CLASS_NM', '')}",
            f"담당교수명: {item.get('PROF_KOR_NM', '')}",
            f"강의유형: {item.get('CLASS_TYPE', '')}",
            f"타학과허가여부: {item.get('ETC_PERMIT_YN', '')}",
            f"복수전공허가여부: {item.get('SEC_PERMIT_YN', '')}",
            "-" * 40
        ]
        result_lines.append("\n".join(lines))

    result_text = "\n".join(result_lines)
    with open("timetable_info.txt", "w", encoding="utf-8") as f:
        f.write(result_text)

    return result_text