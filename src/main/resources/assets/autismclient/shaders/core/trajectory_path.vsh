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
layout(location = 1) noperspective out float edge;
void main() {
    vec4 a = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vec4 b = ProjMat * ModelViewMat * vec4(Position + Normal, 1.0);
    vec3 ndc = a.xyz / a.w;
    vec2 direction = (b.xy / b.w - ndc.xy) * ScreenSize;
    float lengthSquared = dot(direction, direction);
    direction = lengthSquared > 0.000001 ? direction * inversesqrt(lengthSquared) : vec2(1.0, 0.0);
    vec2 offset = vec2(-direction.y, direction.x) * LineWidth / ScreenSize;
    if (offset.x < 0.0) offset *= -1.0;
    edge = (gl_VertexIndex % 2 == 0) ? 1.0 : -1.0;
    gl_Position = vec4((ndc + vec3(offset * edge, 0.0)) * a.w, a.w);
    vertexColor = Color;
}
