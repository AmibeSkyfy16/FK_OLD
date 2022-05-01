package ch.skyfy.fk.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract void readNbt(NbtCompound nbt);

    @Shadow
    public abstract boolean isPlayer();

    @Shadow
    public abstract void calculateDimensions();

    private final AtomicReference<Vec3d> lastPosRef = new AtomicReference<>(null);

    @Inject(at = @At("HEAD"), method = "updateTrackedPosition(Lnet/minecraft/util/math/Vec3d;)V", cancellable = true)
    public void onPlayerMove(Vec3d pos, CallbackInfo callbackInfo) {
        if (0 == 0) {
//            System.out.println("cancelling");
//            callbackInfo.cancel();
            return;
        }
        var livingEntity = (Entity) (Object) this;
        if (livingEntity instanceof ServerPlayerEntity) {
            if (lastPosRef.get() != null) {
                var lastPos = lastPosRef.get();
                if (lastPos.x != pos.x || lastPos.y != pos.y || lastPos.z != pos.z) {

                }
            }
            lastPosRef.set(new Vec3d(pos.x, pos.y, pos.z));
        }
    }

//    @Inject(at = @At("HEAD"), method = "onSpawnPacket", cancellable = true)
//    public void onPlayerMove2(EntitySpawnS2CPacket packet, CallbackInfo callbackInfo){
//        System.out.println("id getBaseClass: " + packet.getEntityTypeId().getBaseClass().getCanonicalName());
//        System.out.println("id getTranslationKey: " + packet.getEntityTypeId().getTranslationKey());
//        System.out.println("moved x: " + packet.getX());
//    }

//    @Inject(at = @At("HEAD"), method = "updateTrackedPositionAndAngles", cancellable = true)
//    public void onPlayerMove2(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate, CallbackInfo callbackInfo) {
////        System.out.println("id getBaseClass: " + packet.getEntityTypeId().getBaseClass().getCanonicalName());
////        System.out.println("id getTranslationKey: " + packet.getEntityTypeId().getTranslationKey());
////        System.out.println("moved x: " + packet.getX());
//        System.out.println("not work");
//    }

//    @Inject(at = @At("HEAD"), method = "updatePosition", cancellable = true)
//    public void onPlayerMove2(double x, double y, double z, CallbackInfo callbackInfo) {
//        System.out.println("cancel");
//        callbackInfo.cancel();
//        System.out.println("updatePosition");
//    }

}
