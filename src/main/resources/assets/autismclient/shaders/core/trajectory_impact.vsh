#version 330
#extension GL_ARB_separate_shader_objects : require
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;
layout(location = 0) out vec2 markerUv;
layout(location = 1) out vec4 vertexColor;
layout(location = 2) flat out int markerStyle;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // All impact shapes share one batch. Style lives in the unused integer part of U.
    markerStyle = int(floor(UV0.x * 0.5));
    markerUv = vec2(UV0.x - float(markerStyle) * 2.0, UV0.y) * 2.0 - 1.0;
    vertexColor = Color;
}
