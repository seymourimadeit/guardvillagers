package tallestegg.guardvillagers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tallestegg.guardvillagers.GuardPacketHandler;
import tallestegg.guardvillagers.GuardVillagers;
import tallestegg.guardvillagers.configuration.GuardConfig;
import tallestegg.guardvillagers.entities.GuardContainer;
import tallestegg.guardvillagers.entities.GuardEntity;
import tallestegg.guardvillagers.networking.GuardFollowPacket;
import tallestegg.guardvillagers.networking.GuardSetPatrolPosPacket;

public class GuardInventoryScreen extends ContainerScreen<GuardContainer> {
    private static final ResourceLocation GUARD_GUI_TEXTURES = new ResourceLocation(GuardVillagers.MODID, "textures/container/inventory.png");
    private static final ResourceLocation GUARD_FOLLOWING_ICON = new ResourceLocation(GuardVillagers.MODID, "textures/container/following_icons.png");
    private static final ResourceLocation GUARD_NOT_FOLLOWING_ICON = new ResourceLocation(GuardVillagers.MODID, "textures/container/not_following_icons.png");
    private static final ResourceLocation PATROL_ICON = new ResourceLocation(GuardVillagers.MODID, "textures/container/patrollingui.png");
    private static final ResourceLocation NOT_PATROLLING_ICON = new ResourceLocation(GuardVillagers.MODID, "textures/container/notpatrollingui.png");
    private final GuardEntity guard;
    private float mousePosX;
    private float mousePosY;
    private boolean buttonPressed;

    public GuardInventoryScreen(GuardContainer p_i51084_1_, PlayerInventory p_i51084_2_, GuardEntity p_i51084_3_) {
        super(p_i51084_1_, p_i51084_2_, p_i51084_3_.getDisplayName());
        this.guard = p_i51084_3_;
        this.titleX = 80;
        this.playerInventoryTitleX = 100;
        this.passEvents = false;
    }

    @Override
    public void init() {
        super.init();
        if (guard.getOwner() != null) {
            this.addButton(new GuardGuiButton(this.guiLeft + 100, this.height / 2 - 40, 20, 18, 0, 0, 19, GUARD_FOLLOWING_ICON, GUARD_NOT_FOLLOWING_ICON, true, (p_214086_1_) -> {
                GuardPacketHandler.INSTANCE.sendToServer(new GuardFollowPacket(guard.getEntityId()));
            }));
        }
        if (GuardConfig.setGuardPatrolHotv && guard.getOwner() != null || !GuardConfig.setGuardPatrolHotv) {
            this.addButton(new GuardGuiButton(this.guiLeft + 120, this.height / 2 - 40, 20, 18, 0, 0, 19, PATROL_ICON, NOT_PATROLLING_ICON, false, (p_214086_1_) -> {
                buttonPressed = !buttonPressed;
                GuardPacketHandler.INSTANCE.sendToServer(new GuardSetPatrolPosPacket(guard.getEntityId(), buttonPressed));
            }));
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUARD_GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
        InventoryScreen.drawEntityOnScreen(i + 51, j + 75, 30, (float) (i + 51) - this.mousePosX, (float) (j + 75 - 50) - this.mousePosY, this.guard);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);
        int health = MathHelper.ceil(guard.getHealth());
        int armor = guard.getTotalArmorValue();
        ITextComponent guardHealthText = new TranslationTextComponent("guardinventory.health", health);
        ITextComponent guardArmorText = new TranslationTextComponent("guardinventory.armor", armor);
        this.font.func_243248_b(matrixStack, guardHealthText, 80.0F, 20.0F, 4210752);
        this.font.func_243248_b(matrixStack, guardArmorText, 80.0F, 30.0F, 4210752);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.mousePosX = (float) mouseX;
        this.mousePosY = (float) mouseY;
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    class GuardGuiButton extends ImageButton {
        private ResourceLocation texture;
        private ResourceLocation newTexture;
        private boolean isFollowButton;

        public GuardGuiButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, ResourceLocation newTexture, boolean isFollowButton, IPressable onPressIn) {
            super(xIn, yIn, widthIn, heightIn, xTexStartIn, yTexStartIn, yDiffTextIn, resourceLocationIn, onPressIn);
            this.texture = resourceLocationIn;
            this.newTexture = newTexture;
            this.isFollowButton = isFollowButton;
        }

        // This is stupid.
        public boolean requirementsForTexture() {
            boolean following = GuardInventoryScreen.this.guard.isFollowing();
            boolean patrol = GuardInventoryScreen.this.guard.isPatrolling();
            return this.isFollowButton ? following : patrol;
        }

        @Override
        public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
            Minecraft minecraft = Minecraft.getInstance();
            ResourceLocation icon = this.requirementsForTexture() ? texture : newTexture;
            minecraft.getTextureManager().bindTexture(icon);
            int i = this.yTexStart;
            if (this.isHovered()) {
                i += this.yDiffText;
            }

            RenderSystem.enableDepthTest();
            blit(matrixStack, this.x, this.y, (float) this.xTexStart, (float) i, this.width, this.height, this.textureWidth, this.textureHeight);
            if (this.isHovered()) {
                this.renderToolTip(matrixStack, mouseX, mouseY);
            }
        }
    }

}