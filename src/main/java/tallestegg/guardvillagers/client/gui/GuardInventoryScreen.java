package tallestegg.guardvillagers.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.common.entities.Guard;
import tallestegg.guardvillagers.common.entities.GuardContainer;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.networking.GuardFollowPacket;
import tallestegg.guardvillagers.networking.GuardSetPatrolPosPacket;

public class GuardInventoryScreen extends AbstractContainerScreen<GuardContainer> {
    private static final Identifier GUARD_GUI_TEXTURES = Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "textures/container/inventory.png");
    private static final WidgetSprites GUARD_FOLLOWING_ICONS = new WidgetSprites(Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "following/following"), Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "following/following_highlighted"));
    private static final WidgetSprites GUARD_NOT_FOLLOWING_ICONS = new WidgetSprites(Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "following/not_following"), Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "following/not_following_highlighted"));
    private static final WidgetSprites GUARD_PATROLLING_ICONS = new WidgetSprites(Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "patrolling/patrolling1"), Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "patrolling/patrolling2"));
    private static final WidgetSprites GUARD_NOT_PATROLLING_ICONS = new WidgetSprites(Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "patrolling/notpatrolling1"), Identifier.fromNamespaceAndPath(GuardVillagers.MODID, "patrolling/notpatrolling2"));
    private static final Identifier ARMOR_EMPTY_SPRITE = Identifier.withDefaultNamespace("hud/armor_empty");
    private static final Identifier ARMOR_HALF_SPRITE = Identifier.withDefaultNamespace("hud/armor_half");
    private static final Identifier ARMOR_FULL_SPRITE = Identifier.withDefaultNamespace("hud/armor_full");

    private final Guard guard;
    private final Player player;
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
        if (GuardConfig.COMMON.followHero.get() && player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || !GuardConfig.COMMON.followHero.get()) {
            this.addRenderableWidget(new GuardGuiButton(this.leftPos + 100, this.height / 2 - 40, 20, 18, GUARD_FOLLOWING_ICONS, GUARD_NOT_FOLLOWING_ICONS, true, btn -> ClientPacketDistributor.sendToServer(new GuardFollowPacket(guard.getId()))
            ));
        }
        if (GuardConfig.COMMON.setGuardPatrolHotv.get() && player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) || !GuardConfig.COMMON.setGuardPatrolHotv.get()) {
            this.addRenderableWidget(new GuardGuiButton(this.leftPos + 120, this.height / 2 - 40, 20, 18, GUARD_PATROLLING_ICONS, GUARD_NOT_PATROLLING_ICONS, false, btn -> {
                buttonPressed = !buttonPressed;
                ClientPacketDistributor.sendToServer(new GuardSetPatrolPosPacket(guard.getId(), buttonPressed));
            }));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                GUARD_GUI_TEXTURES,
                i, j,
                0.0F, 0.0F,
                this.imageWidth, this.imageHeight,
                256, 256
        );
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, this.mousePosX, this.mousePosY, this.guard);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
        super.renderLabels(graphics, x, y);
        int health = Mth.ceil(guard.getHealth());
        int armor = guard.getArmorValue();
        Component guardHealthText = Component.translatable("guardinventory.health", health);
        Component guardArmorText = Component.translatable("guardinventory.armor", armor);
        int yValueWithOrWithoutArmor = armor <= 0 ? 20 : 30;
        if (!GuardConfig.CLIENT.guardInventoryNumbers.get() || guard.getMaxHealth() > 20) {
            graphics.drawString(font, guardHealthText, 80, 30, 4210752, false);
        } else if (guard.getMaxHealth() <= 20) {
            for (int i = 0; i < (guard.getMaxHealth() * 0.5); i++) {
                int heartXValue = i * 8 + 80;
                this.renderHeart(graphics, Gui.HeartType.CONTAINER, heartXValue, yValueWithOrWithoutArmor, false);
            }
            for (int i = 0; i < health / 2; i++) {
                int heartXValue = i * 8 + 80;
                if (health % 2 != 0 && health / 2 == i + 1) {
                    this.renderHeart(graphics, Gui.HeartType.NORMAL, heartXValue, yValueWithOrWithoutArmor, true);
                } else {
                    this.renderHeart(graphics, Gui.HeartType.NORMAL, heartXValue, yValueWithOrWithoutArmor, false);
                }
            }
        }
        if (!GuardConfig.CLIENT.guardInventoryNumbers.get()) {
            graphics.drawString(font, guardArmorText, 80, 20, 4210752, false);
        } else {
            if (armor > 0) {
                for (int k = 0; k < 10; k++) {
                    int l = k * 8 + 80;
                    if (k * 2 + 1 < armor) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR_FULL_SPRITE, l, 20, 9, 9);
                    }

                    if (k * 2 + 1 == armor) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR_HALF_SPRITE, l, 20, 9, 9);
                    }

                    if (k * 2 + 1 > armor) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR_EMPTY_SPRITE, l, 20, 9, 9);
                    }
                }
            }
        }
    }

    private void renderHeart(GuiGraphics guiGraphics, Gui.HeartType heartType, int x, int y, boolean halfHeart) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, heartType.getSprite(false, halfHeart, false), x, y, 9, 9);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        this.mousePosX = (float) mouseX;
        this.mousePosY = (float) mouseY;
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    class GuardGuiButton extends ImageButton {
        private final WidgetSprites texture;
        private final WidgetSprites newTexture;
        private final boolean isFollowButton;

        public GuardGuiButton(int xIn, int yIn, int widthIn, int heightIn, WidgetSprites resourceLocationIn, WidgetSprites newTexture, boolean isFollowButton, OnPress onPressIn) {
            super(xIn, yIn, widthIn, heightIn, resourceLocationIn, onPressIn);
            this.texture = resourceLocationIn;
            this.newTexture = newTexture;
            this.isFollowButton = isFollowButton;
        }

        public boolean requirementsForTexture() {
            return isFollowButton ? guard.isFollowing() : guard.isPatrolling();
        }

        @Override
        public void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            WidgetSprites icon = this.requirementsForTexture() ? this.texture : this.newTexture;
            Identifier sprite = icon.get(this.isActive(), this.isHoveredOrFocused());
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.width, this.height);
        }
    }
}