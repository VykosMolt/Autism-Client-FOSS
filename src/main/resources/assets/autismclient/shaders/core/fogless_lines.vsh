#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:globals.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec3 Normal;
layout(location = 3) in float LineWidth;

layout(location = 0) out vec4 vertexColor;

const float VIEW_SHRINK = 1.0 - (1.0 / 256.0);
const mat4 VIEW_SCALE = mat4(
    VIEW_SHRINK, 0.0, 0.0, 0.0,
    0.0, VIEW_SHRINK, 0.0, 0.0,
    0.0, 0.0, VIEW_SHRINK, 0.0,
    0.0, 0.0, 0.0, 1.0
);

void main() {
    vec4 viewStart = VIEW_SCALE * ModelViewMat * vec4(Position, 1.0);
    vec3 viewDir = (VIEW_SCALE * ModelViewMat * vec4(Normal, 0.0)).xyz;
    float stepLen = min(0.05, max(0.0005, abs(viewStart.z) * 0.25));
    vec4 viewEnd = vec4(viewStart.xyz + viewDir * stepLen, 1.0);

    vec4 linePosStart = ProjMat * viewStart;
    vec4 linePosEnd = ProjMat * viewEnd;

    float w1 = abs(linePosStart.w) < 1e-5 ? (linePosStart.w < 0.0 ? -1e-5 : 1e-5) : linePosStart.w;
    float w2 = abs(linePosEnd.w) < 1e-5 ? (linePosEnd.w < 0.0 ? -1e-5 : 1e-5) : linePosEnd.w;
    vec3 ndc1 = linePosStart.xyz / w1;
    vec3 ndc2 = linePosEnd.xyz / w2;

    vec2 screenDelta = (ndc2.xy - ndc1.xy) * ScreenSize;
    float screenLen = length(screenDelta);
    vec2 lineScreenDirection = screenLen > 1e-5 ? (screenDelta / screenLen) : vec2(1.0, 0.0);
    vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * LineWidth / ScreenSize;

    if (lineOffset.x < 0.0) {
        lineOffset *= -1.0;
    }

    if (gl_VertexIndex % 2 == 0) {
        gl_Position = vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);
    } else {
        gl_Position = vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);
    }

    vertexColor = Color;
}
