// 1. 캔버스 엘리먼트를 가져옴
const canvas = document.getElementById('webglCanvas');
// 2. WebGL2 컨텍스트 가져오기
const gl = canvas.getContext('webgl2');
// 3. 화면 초기화 - 색상 설정
gl.clearColor(0.0, 0.0, 0.0, 1.0); // RGBA: 검은색
// 4. 캔버스 지우기
gl.clear(gl.COLOR_BUFFER_BIT);
