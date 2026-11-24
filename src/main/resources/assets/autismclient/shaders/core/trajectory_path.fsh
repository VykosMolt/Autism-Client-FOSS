#version 330
#extension GL_ARB_separate_shader_objects : require
#include <minecraft:dynamictransforms.glsl>
layout(location = 0) in vec4 vertexColor;
layout(location = 1) noperspective in float edge;
layout(location = 0) out vec4 fragColor;
void main() {
    float aa = max(fwidth(edge), 0.001);
    float coverage = 1.0 - smoothstep(1.0 - aa, 1.0, abs(edge));
    fragColor = vertexColor * ColorModulator;
    fragColor.a *= coverage;
}
