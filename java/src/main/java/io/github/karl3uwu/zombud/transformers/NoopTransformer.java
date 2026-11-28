package io.github.karl3uwu.zombud.transformers;

import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;

public class NoopTransformer extends Transformer {
    @Override
    public Result transform(byte[] classBytes, ClassContext ctx) {
        return NOOP_RESULT;
    }
}
