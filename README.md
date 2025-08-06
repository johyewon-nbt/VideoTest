## Video 전환 테스트

- 비디오 형식이 다를 경우 안드로이드 코드 내에서 전환 및 영상 표시가 가능한지 확인하기 위해 만든 레포입니다.
- Video 형식을 전환하고, MediaPlayer 에서 실행해보는 것이 목적

<br/><br/>

1. MediaPlayer 테스트
- MediaPlayer 에서 변환 없이 직접 플레이

<br/>

2. WebView 테스트
- WebView 에서 각 확장자마다 플레이가 가능한지 확인

<br/>

3. Remux 테스트
- Muxer 를 사용해 `mp4` 로 변환 후 MediaPlayer 에 표시가 가능한지 확인
