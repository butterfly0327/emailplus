# emailplus

## 이메일 OTP 템플릿 적용 안내

- `backend/src/main/resources/templates/otp-email.html`에 루트의 디자인 HTML을 기반으로 한 템플릿을 추가했습니다.
- `backend/src/main/java/com/backend/backend/domain/auth/service/AuthService.java`에서 위 템플릿을 읽어 OTP(4자리)와 만료 시간을 치환해 발송하도록 수정했습니다.
- 이메일 본문에 로고 이미지를 인라인으로 삽입하도록 처리했습니다.

### logo.png 위치

아래 경로에 `logo.png` 파일을 넣으면 인증 이메일에 자동으로 포함됩니다.

```
backend/src/main/resources/email/logo.png
```

`backend/src/main/resources/email/README.md`에도 동일한 안내가 있습니다.
