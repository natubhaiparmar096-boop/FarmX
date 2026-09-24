package com.jelly.farmhelperv2.feature.impl.pest.helpers;

import com.jelly.farmhelperv2.config.FarmHelperConfig;
import com.jelly.farmhelperv2.handler.GameStateHandler;
import com.jelly.farmhelperv2.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.List;

public final class PestEspManager {
    private static final PestEspManager INSTANCE = new PestEspManager();
    private static final Minecraft mc = Minecraft.getMinecraft();

    private PestEspManager() {}

    public static PestEspManager getInstance() {
        return INSTANCE;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!FarmHelperConfig.pestEsp || !GameStateHandler.getInstance().inGarden()) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        List<Entity> pests = PestTargetTracker.getLoadedPests();
        if (pests.isEmpty()) return;

        Color espColor = new Color(255, 60, 60, 180);
        Color tracerColor = new Color(255, 200, 50, 220);
        Color routeColor = new Color(50, 220, 255, 200);

        double vX = mc.getRenderManager().viewerPosX;
        double vY = mc.getRenderManager().viewerPosY;
        double vZ = mc.getRenderManager().viewerPosZ;

        for (Entity pest : pests) {
            if (pest == null || pest.isDead) continue;
            AxisAlignedBB bb = pest.getEntityBoundingBox();
            if (bb != null) {
                AxisAlignedBB renderBB = new AxisAlignedBB(
                        bb.minX - vX, bb.minY - vY, bb.minZ - vZ,
                        bb.maxX - vX, bb.maxY - vY, bb.maxZ - vZ
                );
                RenderUtils.drawBox(renderBB, espColor);
            }

            if (FarmHelperConfig.pestEspTracers) {
                drawTracer(pest.posX, pest.posY + pest.getEyeHeight() * 0.5, pest.posZ, tracerColor, event.partialTicks);
            }
        }

        if (FarmHelperConfig.pestEspRoute && pests.size() > 1) {
            List<Entity> route = PestTargetTracker.buildOptimizedRoute(null);
            drawRouteLines(route, routeColor, vX, vY, vZ);
        }

        // Render particle arc if present
        List<Vec3> arcPoints = PestTrackerAbility.getTrail().getPoints();
        if (arcPoints.size() > 1) {
            drawArc(arcPoints, new Color(255, 120, 0, 220), vX, vY, vZ);
        }
    }

    private static void drawTracer(double x, double y, double z, Color color, float partialTicks) {
        double vX = mc.getRenderManager().viewerPosX;
        double vY = mc.getRenderManager().viewerPosY;
        double vZ = mc.getRenderManager().viewerPosZ;

        Vec3 eye = mc.thePlayer.getPositionEyes(partialTicks);
        Vec3 forward = mc.thePlayer.getLook(partialTicks);
        Vec3 start = eye.addVector(forward.xCoord * 0.5, forward.yCoord * 0.5, forward.zCoord * 0.5);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GL11.glLineWidth(2.0f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        wr.pos(start.xCoord - vX, start.yCoord - vY, start.zCoord - vZ)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        wr.pos(x - vX, y - vY, z - vZ)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();

        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawRouteLines(List<Entity> route, Color color, double vX, double vY, double vZ) {
        if (route == null || route.size() < 2) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GL11.glLineWidth(2.5f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (Entity e : route) {
            wr.pos(e.posX - vX, e.posY + 0.5 - vY, e.posZ - vZ)
                    .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }
        tessellator.draw();

        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawArc(List<Vec3> points, Color color, double vX, double vY, double vZ) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GL11.glLineWidth(3.0f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (Vec3 p : points) {
            wr.pos(p.xCoord - vX, p.yCoord - vY, p.zCoord - vZ)
                    .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        }
        tessellator.draw();

        GL11.glLineWidth(1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
