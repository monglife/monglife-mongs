# stage 통합테스트 — 2026-09-20

`develop` → stage 배포분(미션 기능 전체 + 관리자 백오피스 + 로테이션/게시 잠금)의 실환경 첫 검증.

| | |
|---|---|
| 대상 | stage (`stage.gateway.monglife.cloud`, `stage.common.monglife.cloud`, MQTT 토픽 `mongs-stg`) |
| 앱 | `com.mongs.wear` 2.3.1(33) **stgDebug**, `emulator-5554` (Wear OS 480×480) |
| 계정·몽 | `accountId=1`, `mongId=2` "ㄱ" (시작 시 CH100 별몽 Lv.1 NORMAL) |
| 관리자 웹 | 로컬 `npm run dev:stg` → `http://localhost:5174` |
| 실행 | 2026-09-20 06:25 ~ 06:57 KST |

검증은 **logcat 의 응답 코드·본문과 MQTT 수신**으로 판정했다. Wear Compose 가 semantics 를
내보내지 않아 UI 트리로는 텍스트를 단언할 수 없다. 스크린샷은 화면 전환 확인과 좌표 결정에만 썼다.

---

## 결과 요약

| 구분 | 통과 | 실패 | 미실행·차단 |
|---|---|---|---|
| P0 미션 | 10 | 1 | 1 (로테이션 경계) |
| P0 관리자 | 5 | 1 | 3 |
| P1 게임 루프 | 12 | 1 | 2 |

**발견 결함 7건 + 갭 1건.** 계획대로 코드는 고치지 않았다.

가장 큰 수확 두 가지:

- **M-01 게이트 통과** — `configs/migration/2026-09-17-mission.sql` 과
  `2026-09-15-feedback.sql` 이 stage 에 모두 적용돼 있다. stg 는 `hbm2ddl.auto: none` 이라
  기동 성공이 표 존재를 보장하지 않으므로 이게 첫 확인이다.
- **미션 집계 규칙 3종이 실제 환경에서 정확히 동작한다** — COUNT 는 매번 +1,
  DISTINCT 는 detailCode 로 중복 제거, ACCUMULATE 는 실제 수량을 싣는다. 한 번에
  배변 4개를 치우면 `MS_M_008` 이 +4, 훈련 점수 3점이면 `MS_M_009` 가 +3 이다.

---

## P0 — 미션

| ID | 목적 | 결과 | 근거 |
|---|---|---|---|
| **M-01** | 마이그레이션 적용 (게이트) | **통과** | `GET /api/character/mission` → 200 `MONGS-CHARACTER-MONG-020` |
| M-02 | 일간 선정 불변식 | **통과** | 정확히 5건, actionCode 중복 없음, 전부 DAILY: `MS_D_013/016/018/019/020` |
| M-03 | 주간·월간 그룹 노출 | **통과** | 주간 `MS_W_001~010`, 월간 `MS_M_001~010`, `currentRotationGroup: 0`. 앱 화면도 그룹0 문구("5종·4종")이고 그룹1("8종·6종")이 아님 |
| M-04 | COUNT 진행 | **통과** | 인벤토리 3회 사용 → `MS_D_018` 0→1→2→**3/3 CLAIMABLE** |
| M-05 | DISTINCT 중복 제거 | **통과** | 같은 `FD000` 2회 → `MS_W_005` 는 1/4 에서 멈추고 COUNT 인 `MS_D_018` 만 2로. 다른 `FD010` 추가 → 2/4 `[FD000,FD010]`. 훈련도 같은 `TR000` 재시도는 `MS_W_003` 을 올리지 않음 |
| M-06 | ACCUMULATE | **통과** | 걸음 환전 +300 → `MS_W_008` 0→**300**. 배변 4개 → `MS_M_008` 0→**4**. 훈련 점수 3 → `MS_M_009` +**3**. 티켓 100원 → `MS_W_006` +**100** |
| M-07 | CARE_DAY 하루 1회 | **통과** | 하루에 10여 개 액션을 했지만 `MS_W_009/010` 은 1 에서 불변, `details=[20260920]` 하나 |
| M-08 | 보상 수령 (EXP) | **통과** | `POST /mission/1/claim {"mongId":2}` → 200 `MONGS-CHARACTER-MONG-021`, `expRatio 20→30`, **MQTT `mongs-stg/mong/management/2` 수신**, 목록 `CLAIMED` |
| M-09 | 게시 잠금 | **통과** | 상세에 "게시 중" 배너, `수정` 비활성 + 툴팁, `삭제` 30건 전부 비활성. 강제 호출 PUT/DELETE 모두 `400-102-005` |
| M-10 | 중복 수령 차단 | **통과** | 목록 "수령 완료" 회색, 상세 버튼 비활성, **탭해도 호출이 나가지 않음** |
| **M-11** | 등록 검증 | **부분 실패** | ①②③ 정상, **④ 목표치 중복이 500** → [결함 2](#결함-2) |
| M-12 | 주간 로테이션 경계 | **미실행** | 9/21(월) 00:00 KST 이후에만 가능 |

### M-11 세부

| 케이스 | 실제 |
|---|---|
| ① 교차 주기 (DAILY+FEED_FOOD+DISTINCT) | 409 `400-102-003` ✅ |
| ② 코드 중복 (`MS_W_001`) | 409 `400-102-002` ✅ |
| ③ 리워드 INVENTORY+MAP | 400 `500-102-003` ✅ |
| ④ 목표치 중복 (FEED_FOOD+COUNT+3) | **500 `GLOBAL-ERROR-001`** ❌ |

---

## P0 — 관리자

| ID | 목적 | 결과 | 근거 |
|---|---|---|---|
| A-01 | 미션 목록·게시 표시 | **통과** | 60건, 일간 전부 게시 중, 주간·월간은 그룹0 만 게시 중 |
| A-03 | task 중복 등록 차단 | **통과** | 이미 걸린 `SLEEP` 재등록 → `400-101-101`. 일시중지 행 재사용 NPE 회귀 없음 |
| A-06 | 게시 표시 | **통과** | M-09 과 동일 근거 |
| A-08 | 지수 변경 → 앱 MQTT | **통과** | `PATCH /admin/mongs/2/status {satiety:61,strength:62}` → 200, 즉시 MQTT 푸시 `strengthRatio=62.0, satietyRatio=61.0` |
| — | 관리자 토큰 재발급 | **통과** | 액세스 토큰 30분 만료 후 `POST /public/auth/reissue` → 200 `DISCOVERY-APP-AUTH-003` |
| **A-07** | 스케줄 등록·삭제 | **부분 실패** | 삭제 200, 재등록은 목록이 돌려준 코드로 **불가** → [결함 3](#결함-3) |
| A-02b | DEAD 복구 | **미실행** | 파괴적. 여분 몽이 없어 `mongId=2` 로는 돌리지 않았다 |
| A-04 | 수정이 진행 중 사용자에 즉시 반영 | **차단** | 미션 마스터 수정이 자동 승인 정책에 막혔다(공유 데이터 변경). 사람이 직접 돌려야 한다 |
| A-05 | 삭제 차단(`400-102-004`) | **미실행** | A-04 와 같은 이유. 게시를 내려야 도달하는 경로다 |

> A-04/A-05 절차: `PATCH /admin/missions/16/active {isActive:false}` → `PUT /admin/missions/16`
> 로 `goalCount` 를 현재 진행도 아래로 → 앱 재조회 시 그 자리에서 `CLAIMABLE` 인지 확인 →
> 목표치와 노출 원복. `MS_D_016`(현재 2/2)보다 `MS_D_013`(1/1) 이 되돌리기 쉽다.

---

## P1 — 게임 루프

| 항목 | 결과 | 근거 |
|---|---|---|
| **걸음 수 환전** | **통과** | 계획에서 "에뮬레이터 불가"로 뺐으나 **동작한다**. Health Services 합성 데이터가 이미 쌓여 있다(`StepCollection ... balance=720426 source=HEALTH_STEPS`). 3,000걸음 → `payPoint +300` (1,000걸음당 100) |
| 토큰 만료 복구(앱) | **통과** | 환전 POST 401 `DISCOVERY-GATEWAY-101` → `/api/public/auth/reissue` 200 → 재시도 200. 사용자에게 노출 없음 |
| G-02 음식 섭취 | **통과** | `POST /interaction/food/2 {"foodCode":"FD000"}` → `MONGS-CHARACTER-MONG-005`, payPoint −10, satiety +5, weight +10, MQTT 수신 |
| G-03 쓰다듬기 쿨다운 | **통과** | 1회 성공 후 즉시 재시도 → 400 `400-101-009`, 앱에 "잠시후 가능" |
| G-04 배변 치우기 | **통과** | 4개 → 0, exp +8. `MS_M_008` +4(개수만큼), `MS_M_005/006` +8 |
| G-05 훈련 | **통과** | **실패 경로**: 달리기 score 1 < 20 → `FAIL`, 보상 0, `PAY_POINT_EARN` 미발생(증가분 0이라 `accumulate` 가 건너뜀 — 정상). **성공 경로**: 가위바위보 score 3 → `SUCCESS +30`, `MS_W_008` 300→330, `MS_M_009` +3, `MS_W_003` `[TR000,TR002]`, `MS_M_005/006` +5 |
| G-06 진화 | **통과** | `(관)` exp 100 → 상태가 자동으로 `EVOLUTION_READY` 로 전이. 앱에서 진화 → `MONGS-CHARACTER-MONG-018`, CH100 별몽 → **CH220 유쾌한 별별몽** Lv.2, exp 0. `MS_M_002` 1→2, `MS_M_007` `[CH100,CH220]`. 새 스프라이트 정상 렌더 |
| G-07 수면·기상 | **통과** | 재우기: `DECREASE-STATUS`·`INCREASE-POOP` 제거 + `INCREASE-STATUS#29` 생성. 깨우기: 역방향 복원(`#30`,`#31`). 전부 `isScheduled=true`, 고아 없음 |
| G-08 인벤토리 사용 | **통과** | `POST /interaction/inventory/2 {"inventoryId":N}` → `MONGS-CHARACTER-MONG-008` ×3 |
| G-09 뽑기 · 티켓 구매 | **통과** | 한 번의 "뽑기"가 티켓 구매(−100) → 뽑기 두 단계로 동작. `MP017 이마트24` 획득. `MS_D_016` 1→2/2 CLAIMABLE, `MS_W_004` `[MP017,MP037]`, `MS_W_006/007` +100 |
| **G-10 배틀** | **부분 실패** | 2회차는 전 구간 통과(대기열 → 매칭 → 입장 → 10라운드 → `WIN +200`). 1회차는 `ENTERING` 에서 멈춤 → [결함 7](#결함-7) |
| G-12 오류 신고 | **통과** | `POST https://stage.common.monglife.cloud/api/feedback` → 200 `DISCOVERY-APP-FEEDBACK-000`. discovery `monglife_feedback` 표 적용 확인. 다만 관리자 쪽에 읽을 화면이 없다 → [갭](#갭--관리자-오류-신고-화면이-목이다) |
| 앱 콜드 스타트 | **통과** | 강제 종료 후 재기동 시 로그인·미션·몽 상태 정상 복원 (3회) |
| G-11 도감 | **미실행** | |
| G-13 슬롯·몽 삭제 | **미실행** | 파괴적이라 마지막으로 미뤘다 |

### 에뮬레이터 미검증

`charge/starPoint`(Google Play Billing), `searchMap`(GPS 목킹).
**걸음 수는 검증됐으니 계획의 "못 하는 것" 목록에서 빼도 된다.**

### 관찰 (결함 아님, 의도 확인 필요)

- **미션 보상으로 받은 EXP 는 `EXP_EARN` 에 반영되지 않는다.** `MS_D_018` 수령으로 exp 가
  20→30 이 됐지만 `MS_M_005`(경험치 300 모으기)는 10 에서 안 움직였다. 반면 훈련·배변으로
  얻은 exp 는 반영된다. 보상이 보상을 낳는 고리를 막은 것으로 보이나, 사용자에겐
  "경험치를 얻었는데 경험치 미션이 안 오른다"로 보인다.
- **배틀 보상 페이 포인트도 `PAY_POINT_EARN` 에 안 잡힌다** (+200 인데 `MS_W_008` 불변).
  계획에 "배틀은 미션에 안 물려 있다"고 적혀 있어 의도대로다.

---

## 결함

> **조치 현황 (2026-09-21)** — 결함 1~5 와 7 은 수정하고 **dev 에서 검증 완료**했다.
> 결함 6(앱 "수령 완" 잘림)은 wear 릴리스 주기가 달라 다음 릴리스로 미뤘다.
> 수정하며 새로 드러난 것 둘을 같이 고쳤다.
>
> | 결함 | 상태 |
> |---|---|
> | 1 게시 기간 배열 직렬화 | 수정 — `@JsonFormat(pattern = "yyyy-MM-dd")` |
> | 2 목표치 중복 500 | 수정 — 등록 경로에 선검사 + 전용 코드 `400-102-006` |
> | 3 스케줄 코드 불일치 | 수정 — `MongSchedulerType.fromCode` 가 코드·이름 둘 다 받고, 관리자 웹은 하이픈 코드로 통일 |
> | 4 인벤토리 건수 | 수정 — 관리자 전용 포트가 `Page.getTotalElements()` 를 넘긴다 |
> | 5 에러 코드 중복 | 수정 — `ALREADY_EXISTS_MASTER_CODE` → `400-101-105` |
> | 6 "수령 완" 잘림 | 보류 (다음 wear 릴리스) |
> | 7 배틀 `ENTERING` 고착 | 수정 — 30초 기한 스위퍼 + `CANCELED` 상태 + 참가비 환불 |
>
> **수정 중 새로 찾은 것**
> - 본문 파싱 실패(`HttpMessageNotReadableException`)에 핸들러가 없어 관리자 모듈의 모든
>   `@RequestBody` 가 응답 코드 없는 Spring 기본 봉투를 냈다. 결함 3 의 "올바른 코드와
>   없는 코드의 응답이 똑같다" 증상의 실제 원인이다. 핸들러를 추가했다.
> - `CANCELED` 를 `END` 로 합쳤다면 `exitMatchUseCase`/`pickMatchUseCase` 의 가드가
>   `isEnd()` 기준이라 **취소된 매치에 +200 승리 보상이 나갈 수 있었다.** `isTerminal()` 로 바꿨다.
> - `Match.getWinner()` 가 입장하지 않은 사람 둘의 체력이 같으면 `exitedAt` null 로 NPE 였다.
>   null 가드를 넣었다.
> - 미션 **수정** 경로도 목표치 충돌에 "다른 주기에 있다"는 틀린 메시지를 내고 있었다. 같이 맞췄다.
>
> 배포 전에 `configs/migration/2026-09-21-match-cancel.sql` 을 **dev 에도** 돌려야 한다.
> Hibernate 의 `update` 는 기존 ENUM 컬럼에 값을 추가하지 못한다. (dev 적용 완료)

### dev 검증 결과 (2026-09-21 11:37~12:00)

환경: dev 백엔드(로컬 8000/8010, DB `monglife_mongs_dev`), 에뮬레이터 `emulator-5554` 에
**devDebug** 재설치, 관리자 웹 `npm run dev` (5175). dev 계정은 `accountId=4`,
`mongId=1` "ㄱ" CH311 Lv.3. 관리자는 `admin@monglife.cloud`.

| 결함 | 검증 | 결과 |
|---|---|---|
| 1 날짜 | `periodStart` 가 `"2026-09-20"` 문자열. 화면도 `2026-09-14 ~ 2026-09-20` | 통과 |
| 2 미션 등록 | `MS_D_001` 과 같은 `(FEED_FOOD, COUNT, 3)` 등록 → **409 `400-102-006`**. 교차 주기는 `400-102-003` 유지 | 통과 |
| 3 스케줄 코드 | 목록이 준 하이픈 코드(`DECREASE-STATUS`)로 삭제→재등록 성공(`taskId 17→19`). enum 이름도 받음. 오타는 `400-101-106`, 깨진 JSON 도 코드 있는 봉투. **화면 라벨 "배변 증가" 정상**, 등록 모달이 안 걸린 3종만 제시 | 통과 |
| 4 인벤토리 건수 | `size` 3/7/50 전부 `X-Total-Count=2`(실제 2건). 화면 "총 2건" | 통과 |
| 5 에러 코드 | 스케줄 중복 `400-101-101`, 마스터 코드 중복 **`400-101-105`** 로 분리 | 통과 |
| 7 배틀 | 아래 표 | 통과 |

배틀 세 경로를 모두 돌렸다. payPoint 로 정산이 검증된다.

| 시나리오 | payPoint | 매치 상태 |
|---|---|---|
| 입장 안 함 → 기한 초과 (스위퍼) | 1700 → 1650 → **1700** | `#1 CANCELED round=0` |
| 정상 배틀 승리 | 1700 → 1650 → **1850** | `#2 END round=9` |
| 입장 안 함 → 관리자 강제 종료 | 1800 → **1850** | `#3 CANCELED round=0` |

- 환불은 `CANCELED` 전이 안에서만 일어나므로 상태 전이도 함께 확인된다. 몽 `updatedAt` 이
  매칭 36초 뒤로 찍혔다 — 기한 30초 + 스위퍼 주기 5초와 맞는다.
- 정상 배틀의 승리 보상이 그대로라 `isTerminal()` 가드가 기존 흐름을 깨지 않았다.
- `CANCELED` 가 DB 에 저장된다 = 마이그레이션의 varchar 전환이 먹었다. ENUM 이었으면
  `Data truncated` 로 터졌을 자리다.

앱 회귀도 같이 봤다 — 로그인·기기 등록·몽 선택·수면/기상 전환·미션 목록(일간 5건, 진행도와
`CLAIMABLE` 전이)·배틀 10라운드 전부 정상이다.

### 결함 1 — 미션 게시 기간이 배열로 직렬화된다

관리자 목록/상세에서 게시 기간이 `2026,9,20 ~ 2026,9,20` 으로 나온다. 실제 응답:

```json
"periodStart": [2026, 9, 20], "periodEnd": [2026, 9, 20]
```

`AdminMissionResponseDto.periodStart` / `periodEnd` 는 **이 프로젝트에서 유일하게
`@JsonFormat` 이 없는 날짜 필드**다. 웹 계층에 전역 Jackson 날짜 설정이 없다.

- 위치: `character-service/adapter-in/admin-web-adapter-in/.../dto/response/AdminMissionResponseDto.java`
- 영향: 표시만. 데이터는 정상
- 고치는 법: 다른 날짜 필드와 같이 `@JsonFormat(shape = STRING, pattern = "yyyy-MM-dd")`

### 결함 2 — 목표치가 중복된 미션을 만들면 원시 500 이 샌다

`(액션, 목표타입, 목표치)` 가 기존 미션과 겹치는 미션을 등록하면 DB 유니크 키
`uk_mission_goal` 이 그대로 터져 `500 GLOBAL-ERROR-001` 이 나간다. 운영자는 무엇이
잘못됐는지 알 수 없다.

- 원인: `AdminMissionService.createMissionUseCase` 는 `isExistsMissionCodePort` 와
  `isExistsGoalInOtherCyclePort`(교차 주기)만 본다. 같은 주기 안의 목표치 중복을 보는
  `isExistsGoalPort` 는 **수정 경로에만** 있고 등록 경로에 없다
- 재현: `POST /admin/missions` 로 DAILY + `FEED_FOOD` + `COUNT` + `goalCount=3` (= `MS_D_001`)
- 고치는 법: 등록 경로에도 `isExistsGoalPort` 선검사를 넣고 `400-102-003` 로 돌려준다

### 결함 3 — 스케줄 코드가 읽기와 쓰기에서 다르다

`MongSchedulerType` 은 enum 이름(`DECREASE_STATUS`)과 코드 문자열(`DECREASE-STATUS`)이
다른데, **목록 응답은 코드(하이픈)를 주고 등록 요청은 enum 이름(언더스코어)을 받는다.**
7종 중 `SLEEP`/`WAKEUP`/`DEAD` 만 우연히 같아서 안 드러난다.

확인된 증상 셋:

1. 목록이 준 값을 그대로 등록에 쓰면 **존재하지 않는 코드와 똑같이 거부**된다 —
   `{"timestamp":…,"status":400,"error":"Bad Request"}`. 애플리케이션 에러 봉투조차 없어
   클라이언트가 원인을 알 수 없다
2. 관리자 화면의 스케줄 라벨이 깨진다 — `SCHEDULER_LABEL['DECREASE-STATUS']` 가 없어
   `INCREASE-POOP` 처럼 원시 코드가 그대로 보인다
3. 등록 모달의 중복 필터가 무력화된다 — 이미 걸린 `지수 감소`·`배변 증가`가 계속 후보에
   뜨고, 고르면 `400-101-101` 을 맞는다. "이미 걸림" 안내도 원시 코드로 나온다

- 위치: `.../port/enums/MongSchedulerType.java`, 응답 DTO, 프론트 `mongs/taskLabels.ts`
- 영향: 4/7 타입(`EGG_EVOLUTION`, `INCREASE_STATUS`, `DECREASE_STATUS`, `INCREASE_POOP`)
- 고치는 법: 한쪽으로 통일한다. 응답이 enum 이름을 주게 하거나, 요청에 `@JsonCreator` 로
  코드 문자열을 받게 한다

### 결함 4 — 인벤토리 전체 건수가 가짜다

`X-Total-Count` 가 실제 건수가 아니라 `totalPage * size` 다. `size` 를 바꾸면 헤더가 그대로
따라간다(3→3, 7→7, 50→50). 실제 인벤토리는 2건이었다. 관리자 화면은 이 헤더를 그대로 믿는다.

- 위치: [`AdminMongController.java:180`](../../character-service/adapter-in/admin-web-adapter-in/src/main/java/com/monglife/mongs/adapter/in/admin/character/web/controller/AdminMongController.java)
- 다른 목록(`/admin/mongs` 등)은 정상 — 인벤토리만 그렇다
- 원인: `getInventoriesUseCase` 가 돌려주는 `PageResult` 에 전체 건수가 없다

### 결함 5 — 서로 다른 두 오류가 같은 코드를 쓴다

`ALREADY_EXISTS_TASK` 와 `ALREADY_EXISTS_MASTER_CODE` 가 둘 다 `400-101-101` 이다.
클라이언트가 코드로 분기할 수 없다. (이번 배포 이전부터 있던 문제)

### 결함 6 — 앱 "수령 완료" 버튼 글자가 잘린다

수령한 미션 상세의 비활성 버튼이 `수령 완` 으로 잘려 나온다. 동작은 정상.

### 결함 7 — 배틀 매치가 `ENTERING` 에서 멈춘다 (간헐)

매칭까지는 성공하는데 앱이 **입장 이벤트를 발행하지 않고** 화면이 "매치 입장중" 에서
멈춘다. 서버 매치는 `ENTERING` 으로 남고 정리되지 않는다.

```
06:53:22  POST /battle/queue/2        → MONGS-CHARACTER-BATTLE-000
06:53:32  MQTT battle/queue/<device>  → MONGS-CHARACTER-BATTLE-500 배틀 매칭에 성공했습니다
          (이후 매치 토픽 구독도, 메시지 전송도 없음)
```

정상 동작(2회차)은 이렇게 이어진다:

```
06:55:50  MQTT >> 토픽 구독
06:55:52  MQTT >> 메시지 전송            ← publishMatchEnter
06:55:52  MQTT battle/match/3          → MONGS-CHARACTER-BATTLE-502 모든 플레이어가 입장했습니다
```

- **참가비 50 페이 포인트가 그대로 사라진다** (1100 → 1050, 배틀 없음, 환불 없음).
  계산: 1100 −50(멈춘 매치) −50(정상 매치) +200(승리) = 1200 — 실제 값과 일치
- 서버에 **입장 타임아웃/정리가 없다.** 매치 #2 는 4분 뒤에도 `ENTERING`,
  `updatedAt` 이 생성 시각 그대로다
- 앱 강제 종료 후 재시도하면 정상 동작한다. 재현 조건은 특정하지 못했다 —
  해당 앱 세션은 오래 떠 있었고 뒤로가기로 여러 번 빠져나온 상태였다
- 앱에도 취소 수단이 없다. 사용자는 앱을 죽이는 것 말고 할 게 없다

### ~~갭 — 관리자 오류 신고 화면이 목이다~~ → **오보. 정상 구현돼 있다**

**이 항목은 틀렸다.** 백엔드에 `AdminFeedbackController`(`/admin/error-reports`)가 목록·상세·
답변까지 다 있고, 관리자 웹도 `features/error-reports` 에 api·queries·목록·상세·답변이
전부 구현돼 있다. dev 에서 `GET /api/admin/error-reports` 가 200 으로 실제 신고 1건과
답변까지 돌려준다.

오판의 원인은 둘이다. `features/error-reports/types.ts` 첫 줄에 **"백엔드에 아직 없는 도메인
— 목 계약"** 이라는 낡은 주석이 남아 있었고(백엔드가 나중에 붙으며 주석만 안 고쳐졌다),
확인 grep 을 `features/error-reports` 밖으로 넓히지 않았다. 주석은 바로잡았다.

---

## 남은 일

1. **M-12 주간 로테이션 경계 — 9/21(월) 00:00 KST 이후. 이번 주가 지나면 다음은 일주일 뒤다.**
   그룹0(`MS_W_001~010`) → 그룹1(`MS_W_011~020`) 교체와, 새 `cycleKey` 라 진행도가 0 부터인지.
   그룹1 은 숫자로 구분된다 — 그룹0 은 "5종·4종·500·5일", 그룹1 은 "8종·6종·800·3일".
   일간도 같은 시점에 새 5건으로 갈린다.
2. **A-04 / A-05** — 미션 마스터를 건드려야 해서 이번에 못 했다. 위 P0 표의 절차 참고.
3. A-02b(DEAD 복구), G-11(도감), G-13(슬롯·몽 삭제).
4. 결함 7 재현 조건 특정.

## 테스트가 바꾼 상태 (다음 세션 기준선)

`mongId=2` 는 **CH220 유쾌한 별별몽 Lv.2** 가 됐다 (테스트 전 CH100 별몽 Lv.1).

| | 값 |
|---|---|
| payPoint | **1200** (시작 880) |
| exp | 10 / 진화로 한 번 0 리셋됨 |
| 걸음 잔고 | −3,000 |
| 인벤토리 | 0건 (FD000 ×2, FD010 ×1 소진 — FD010 은 테스트용 지급분) |
| 스케줄 | `SLEEP#17 WAKEUP#18 DECREASE-STATUS#30 INCREASE-POOP#31` (수면 전환으로 #28→#29→#30/31 로 교체됨) |

미션 진행도:

```
일간  MS_D_013 1/1 CLAIMABLE   MS_D_016 2/2 CLAIMABLE   MS_D_018 3/3 CLAIMED
      MS_D_019 1/1 CLAIMED     MS_D_020 1/1 CLAIMABLE
주간  MS_W_001 1/5 [FD000]     MS_W_003 2/3 [TR000,TR002]  MS_W_004 2/5 [MP017,MP037]
      MS_W_005 2/4 [FD000,FD010]  MS_W_006 210/500  MS_W_007 210/1500  MS_W_008 330/500
      MS_W_009 1/5 [20260920]  MS_W_010 1/7 [20260920]   MS_W_002 0/4
월간  MS_M_002 2/3  MS_M_005 52/300  MS_M_006 52/800  MS_M_007 2/3 [CH100,CH220]
      MS_M_008 4/100  MS_M_009 6/500  MS_M_010 1/20 [20260920]
```

그 밖에:

- 배틀 매치 **#2 가 `ENTERING` 으로 남아 있다** (결함 7). 정리 필요 여부 판단 대상
- 오류 신고 1건 등록 (`인증` / `QAtest0920`)

**마스터 데이터는 건드리지 않았다.** 미션 60건·리워드 93건 그대로고, M-11 의 등록 시도는
전부 실패하도록 설계해서 새로 생긴 미션이 없다.
