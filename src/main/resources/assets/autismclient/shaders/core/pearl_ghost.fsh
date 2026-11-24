#version 330
#extension GL_ARB_separate_shader_objects : require
#include <minecraft:dynamictransforms.glsl>
#ifdef PER_FACE_LIGHTING
layout(location = 2) in vec4 vertexPerFaceColorBack;
layout(location = 3) in vec4 vertexPerFaceColorFront;
#else
layout(location = 2) in vec4 vertexColor;
#endif
layout(location = 0) out vec4 fragColor;
void main() {
#ifdef PER_FACE_LIGHTING
    vec4 tint = (gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack) * ColorModulator;
#else
    vec4 tint = vertexColor * ColorModulator;
#endif
    // Untextured volume: white by default, or only the account name's color, with caller-owned opacity.
    fragColor = tint;
}
