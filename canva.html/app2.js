// 1. 캔버스와 WebGL2 컨텍스트 설정
const canvas = document.getElementById('webglCanvas');
const gl = canvas.getContext('webgl2');

// 2. 셰이더 소스 작성 (버텍스 셰이더와 프래그먼트 셰이더)
const vertexShaderSource = `#version 300 es
in vec4 a_position;
void main() {
  gl_Position = a_position; // 정점의 위치를 설정
}`;

const fragmentShaderSource = `#version 300 es
precision highp float;
out vec4 outColor;
void main() {
  outColor = vec4(1.0, 0.0, 0.0, 1.0); // 빨간색 출력
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

// 5. 셰이더 컴파일 및 프로그램 생성
const vertexShader = createShader(gl, gl.VERTEX_SHADER, vertexShaderSource);
const fragmentShader = createShader(gl, gl.FRAGMENT_SHADER, fragmentShaderSource);
const program = createProgram(gl, vertexShader, fragmentShader);
gl.useProgram(program);

// 6. 삼각형 정점 데이터 정의
const positions = [
   0.0,  0.5,  // 위쪽 정점
  -0.5, -0.5,  // 왼쪽 아래 정점
   0.5, -0.5   // 오른쪽 아래 정점
];

// 7. 버퍼 생성 및 데이터 전달
const positionBuffer = gl.createBuffer();
gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(positions), gl.STATIC_DRAW);

// 8. 위치 어트리뷰트 설정
const positionAttributeLocation = gl.getAttribLocation(program, 'a_position');
gl.enableVertexAttribArray(positionAttributeLocation);
gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer);
gl.vertexAttribPointer(positionAttributeLocation, 2, gl.FLOAT, false, 0, 0);

// 9. 화면 초기화 및 삼각형 그리기
gl.clearColor(0.8, 0.8, 0.8, 1.0); // 배경을 검은색으로 설정
gl.clear(gl.COLOR_BUFFER_BIT); // 화면을 검은색으로 초기화
gl.drawArrays(gl.TRIANGLES, 0, 3); // 삼각형 그리기
