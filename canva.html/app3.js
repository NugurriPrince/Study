// 1. 캔버스와 WebGL2 컨텍스트 설정
const canvas = document.getElementById('webglCanvas');
const gl = canvas.getContext('webgl2');

// 2. 셰이더 소스 작성 (버텍스 셰이더와 프래그먼트 셰이더)
const vertexShaderSource = `#version 300 es
in vec4 a_position;        // 입력 정점 좌표
uniform mat4 u_rotation;   // 회전 변환을 위한 4x4 행렬 유니폼
void main() {
    gl_Position = u_rotation * a_position;  // 회전 변환 적용 후 정점 위치 설정
}`; 
const fragmentShaderSource = `#version 300 es
precision highp float;
uniform vec4 u_color;      // 유니폼 변수를 통한 색상 값
out vec4 outColor;         // 출력 색상
void main() {
    outColor = u_color;    // 유니폼으로 전달받은 색상 설정
}`;

// 3. 셰이더 컴파일 함수
function createShader(gl, type, source) {
    const shader = gl.createShader(type);
    gl.shaderSource(shader, source);
    gl.compileShader(shader);
    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
      console.error('Error compiling shader:', gl.getShaderInfoLog(shader));
      gl.deleteShader(shader);
      return null;
    }
    return shader;
}
  
  // 4. 셰이더 프로그램 생성 함수
  function createProgram(gl, vertexShader, fragmentShader) {
    const program = gl.createProgram();
    gl.attachShader(program, vertexShader);
    gl.attachShader(program, fragmentShader);
    gl.linkProgram(program);
    if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
      console.error('Error linking program:', gl.getProgramInfoLog(program));
      gl.deleteProgram(program);
      return null;
    }
    return program;
}

const vertexShader = createShader(gl, gl.VERTEX_SHADER, vertexShaderSource);
const fragmentShader = createShader(gl, gl.FRAGMENT_SHADER, fragmentShaderSource);
const program = createProgram(gl, vertexShader, fragmentShader);
gl.useProgram(program);

// 5. 삼각형 정점 데이터 정의
const positions = [
   0.0,  0.5,  // 위쪽 정점
  -0.5, -0.5,  // 왼쪽 아래 정점
   0.5, -0.5   // 오른쪽 아래 정점
];

// 6. 버퍼 생성 및 데이터 전달
const positionBuffer = gl.createBuffer();
gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(positions), gl.STATIC_DRAW);

// 7. 위치 어트리뷰트 설정
const positionAttributeLocation = gl.getAttribLocation(program, 'a_position');
gl.enableVertexAttribArray(positionAttributeLocation);
gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
gl.vertexAttribPointer(positionAttributeLocation, 2, gl.FLOAT, false, 0, 0);

// 8. 유니폼 위치 가져오기 (색상 및 회전 행렬)
const colorUniformLocation = gl.getUniformLocation(program, 'u_color');
const rotationUniformLocation = gl.getUniformLocation(program, 'u_rotation');

// 9. 삼각형 색상 설정 (초기값: 빨간색)
gl.uniform4f(colorUniformLocation, 1.0, 0.0, 0.0, 1.0);  // 빨간색 (RGBA)

// 10. 화면 초기화 및 삼각형 그리기
gl.clearColor(0.8, 0.8, 0.8, 1.0); // 배경색 설정 (회색)
gl.clear(gl.COLOR_BUFFER_BIT);

// 회전 변환을 위한 행렬 설정
let angle = 0;
function drawScene() {
    angle += 0.01;  // 회전 각도 증가
    const cosA = Math.cos(angle);
    const sinA = Math.sin(angle);

    // 11. 회전 행렬 정의 (2D 회전을 위한 간단한 4x4 행렬)
    const rotationMatrix = [
        cosA, -sinA, 0.0, 0.0,
        sinA,  cosA, 0.0, 0.0,
        0.0,   0.0,  1.0, 0.0,
        0.0,   0.0,  0.0, 1.0
    ];

    // 12. 회전 변환 행렬을 유니폼 변수로 전달
    gl.uniformMatrix4fv(rotationUniformLocation, false, new Float32Array(rotationMatrix));

    // 13. 삼각형 그리기
    gl.clear(gl.COLOR_BUFFER_BIT);
    gl.drawArrays(gl.TRIANGLES, 0, 3);

    // 14. 애니메이션을 위한 프레임 요청
    requestAnimationFrame(drawScene);
}

// 15. 애니메이션 시작
drawScene();
