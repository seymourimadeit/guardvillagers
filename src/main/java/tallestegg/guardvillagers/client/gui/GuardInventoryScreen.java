package tallestegg.guardvillagers.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import tallestegg.guardvillagers.GuardPacketHandler;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.Guard;
import tallestegg.guardvillagers.entities.GuardContainer;
import tallestegg.guardvillagers.networking.GuardFollowPacket;
import tallestegg.guardvillagers.networking.GuardSetPatrolPosPacket;

public class GuardInventoryScreen extends AbstractContainerScreen<GuardContainer> {
    private static final ResourceLocation GUARD_GUI_TEXTURES = new ResourceLocation(GuardVillagers.MODID, "textures/container/inventory.png");
    private static final WidgetSprites GUARD_FOLLOWING_ICONS = new WidgetSprites(new ResourceLocation(GuardVillagers.MODID, "following/following"), new ResourceLocation(GuardVillagers.MODID, "following/following_highlighted"));
    private static final WidgetSprites GUARD_NOT_FOLLOWING_ICONS = new WidgetSprites(new ResourceLocation(GuardVillagers.MODID, "following/not_following"), new ResourceLocation(GuardVillagers.MODID, "following/not_following_highlighted"));
    private static final WidgetSprites GUARD_PATROLLING_ICONS = new WidgetSprites(new ResourceLocation(GuardVillagers.MODID, "patrolling/patrolling1"), new ResourceLocation(GuardVillagers.MODID,"patrolling/patrolling2"));
    private static final WidgetSprites GUARD_NOT_PATROLLING_ICONS = new WidgetSprites(new ResourceLocation(GuardVillagers.MODID, "patrolling/notpatrolling1"), new ResourceLocation(GuardVillagers.MODID,"patrolling/notpatrolling2"));
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
            this.addRenderableWidget(new GuardGuiButton(this.leftPos + 100, this.height / 2 - 40, 20, 18,  GUARD_FOLLOWING_ICONS, GUARD_NOT_FOLLOWING_ICONS, true, (p_214086_1_) -> {
                PacketDistributor.SERVER.noArg().send(new GuardFollowPacket(guard.getId()));
            }));
        }
        if (GuardConfig.setGuardPatrolHotv && player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || !GuardConfig.setGuardPatrolHotv) {
            this.addRenderableWidget(new GuardGuiButton(this.leftPos + 120, this.height / 2 - 40, 20, 18, GUARD_PATROLLING_ICONS, GUARD_NOT_PATROLLING_ICONS, false, (p_214086_1_) -> {
                buttonPressed = !buttonPressed;
                PacketDistributor.SERVER.noArg().send(new GuardSetPatrolPosPacket(guard.getId(), buttonPressed));
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
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, this.mousePosX, this.mousePosY, this.guard);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);
        int health = Mth.ceil(guard.getHealth());
        int armor = guard.getArmorValue();
        Component guardHealthText = Component.translatable("guardinventory.health", health);
        Component guardArmorText = Component.translatable("guardinventory.armor", armor);
        graphics.drawString(font, guardHealthText, 80, 20, 4210752, false);
        graphics.drawString(font, guardArmorText, 80, 30, 4210752, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseX, partialTicks);
        this.mousePosX = (float) mouseX;
        this.mousePosY = (float) mouseY;
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    class GuardGuiButton extends ImageButton {
        private WidgetSprites texture;
        private WidgetSprites newTexture;
        private boolean isFollowButton;

        public GuardGuiButton(int xIn, int yIn, int widthIn, int heightIn, WidgetSprites resourceLocationIn, WidgetSprites newTexture, boolean isFollowButton, OnPress onPressIn) {
            super(xIn, yIn, widthIn, heightIn, resourceLocationIn, onPressIn);
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
            WidgetSprites icon = this.requirementsForTexture() ? this.texture : this.newTexture;
            ResourceLocation resourcelocation = icon.get(this.isActive(), this.isHoveredOrFocused());
            graphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.width, this.height);
        }
    }

}