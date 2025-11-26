package tallestegg.guardvillagers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import tallestegg.guardvillagers.GuardPacketHandler;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;
import tallestegg.guardvillagers.entities.GuardContainer;
import tallestegg.guardvillagers.networking.GuardFollowPacket;
import tallestegg.guardvillagers.networking.GuardSetPatrolPosPacket;

public class GuardInventoryScreen extends AbstractContainerScreen<GuardContainer> {
    private static final ResourceLocation GUARD_GUI_TEXTURES = new ResourceLocation(GuardVillagers.MODID, "textures/container/inventory.png");
    private static final ResourceLocation GUARD_FOLLOWING_ICON = new ResourceLocation(GuardVillagers.MODID, "textures/container/following_icons.png");
    private static final ResourceLocation GUARD_NOT_FOLLOWING_ICON = new ResourceLocation(GuardVillagers.MODID, "textures/container/not_following_icons.png");
    private static final ResourceLocation PATROL_ICON = new ResourceLocation(GuardVillagers.MODID, "textures/container/patrollingui.png");
    private static final ResourceLocation NOT_PATROLLING_ICON = new ResourceLocation(GuardVillagers.MODID, "textures/container/notpatrollingui.png");
    protected static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private final Guard guard;
    private Player player;
    private float mousePosX;
    private float mousePosY;
    private boolean buttonPressed;

    public GuardInventoryScreen(GuardContainer container, Inventory playerInventory, Guard guard) {
        super(container, playerInventory, guard.getDisplayName());
        this.guard = guard;
        this.titleLabelX = 80;
        this.inventoryLabelX = 100;
        this.player = playerInventory.player;
    }

    @Override
    public void init() {
        super.init();
        if (GuardConfig.followHero && player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || !GuardConfig.followHero) {
            this.addRenderableWidget(new GuardGuiButton(this.leftPos + 100, this.height / 2 - 40, 20, 18, 0, 0, 19, GUARD_FOLLOWING_ICON, GUARD_NOT_FOLLOWING_ICON, true, (p_214086_1_) -> {
                GuardPacketHandler.INSTANCE.sendToServer(new GuardFollowPacket(guard.getId()));
            }));
        }
        if (GuardConfig.setGuardPatrolHotv && player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || !GuardConfig.setGuardPatrolHotv) {
            this.addRenderableWidget(new GuardGuiButton(this.leftPos + 120, this.height / 2 - 40, 20, 18, 0, 0, 19, PATROL_ICON, NOT_PATROLLING_ICON, false, (p_214086_1_) -> {
                buttonPressed = !buttonPressed;
                GuardPacketHandler.INSTANCE.sendToServer(new GuardSetPatrolPosPacket(guard.getId(), buttonPressed));
            }));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUARD_GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(GUARD_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, i + 51, j + 75, 30, (float) (i + 51) - this.mousePosX, (float) (j + 75 - 50) - this.mousePosY, this.guard);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);
        int health = Mth.ceil(guard.getHealth());
        int armor = guard.getArmorValue();
        int yValueWithOrWithoutArmor = armor <= 0 ? 20 : 30;
        Component guardHealthText = Component.translatable("guardinventory.health", health);
        Component guardArmorText = Component.translatable("guardinventory.armor", armor);
        if (!GuardConfig.CLIENT.guardInventoryNumbers.get() || guard.getMaxHealth() > 20) {
            graphics.drawString(font, guardHealthText, 80, yValueWithOrWithoutArmor, 4210752, false);
        } else if (guard.getMaxHealth() <= 20) {
            for (int i = 0; i < 10; i++) {
                int heartXValue = i * 8 + 80;
                this.renderHeart(graphics, Gui.HeartType.CONTAINER, heartXValue, yValueWithOrWithoutArmor, 0, false, false);
            }
            for (int i = 0; i < health / 2; i++) {
                int heartXValue = i * 8 + 80;
                if (health % 2 != 0 && health / 2 == i + 1) {
                    this.renderHeart(graphics, Gui.HeartType.NORMAL, heartXValue, yValueWithOrWithoutArmor, 0, false, true);
                } else {
                    this.renderHeart(graphics, Gui.HeartType.NORMAL, heartXValue, yValueWithOrWithoutArmor, 0, false, false);
                }
            }
        }
        if (!GuardConfig.CLIENT.guardInventoryNumbers.get()) {
            graphics.drawString(font, guardArmorText, 80, 20, 2, false);
        } else {
            if (armor > 0) {
                for (int k = 0; k < 10; k++) {
                    int l = k * 8 + 80;
                    if (k * 2 + 1 < armor) {
                        graphics.blit(GUI_ICONS_LOCATION, l, 20, 34, 9, 9, 9);
                    }

                    if (k * 2 + 1 == armor) {
                        graphics.blit(GUI_ICONS_LOCATION, l, 20, 25, 9, 9, 9);
                    }

                    if (k * 2 + 1 > armor) {
                        graphics.blit(GUI_ICONS_LOCATION, l, 20, 16, 9, 9, 9);
                    }
                }
            }
        }
    }

    private void renderHeart(GuiGraphics pGuiGraphics, Gui.HeartType pHeartType, int pX, int pY, int pYOffset,
                             boolean pRenderHighlight, boolean pHalfHeart) {
        pGuiGraphics.blit(GUI_ICONS_LOCATION, pX, pY, pHeartType.getX(pHalfHeart, pRenderHighlight), pYOffset, 9, 9);
    }


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        this.mousePosX = (float) mouseX;
        this.mousePosY = (float) mouseY;
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    class GuardGuiButton extends ImageButton {
        private ResourceLocation texture;
        private ResourceLocation newTexture;
        private boolean isFollowButton;

        public GuardGuiButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, ResourceLocation newTexture, boolean isFollowButton, OnPress onPressIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, onPressIn);
            this.texture = resourceLocationIn;
            this.newTexture = newTexture;
            this.isFollowButton = isFollowButton;
        }

        public boolean requirementsForTexture() {
            boolean following = GuardInventoryScreen.this.guard.isFollowing();
            boolean patrol = GuardInventoryScreen.this.guard.isPatrolling();
            return this.isFollowButton ? following : patrol;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            ResourceLocation icon = this.requirementsForTexture() ? texture : newTexture;
            RenderSystem.setShaderTexture(0, icon);
            int i = this.yTexStart;
            if (this.isHoveredOrFocused()) {
                i += this.yDiffTex;
            }

            RenderSystem.enableDepthTest();
            graphics.blit(icon, this.getX(), this.getY(), (float) this.xTexStart, (float) i, this.width, this.height, this.textureWidth, this.textureHeight);
        }
    }

}