package net.onixary.shapeShifterCurseFabric.custom_ui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.onixary.shapeShifterCurseFabric.custom_ui.util.ScaledLabelComponent;
import net.onixary.shapeShifterCurseFabric.data.CodexData;
import net.onixary.shapeShifterCurseFabric.player_form.ability.PlayerFormComponent;
import net.onixary.shapeShifterCurseFabric.player_form.ability.RegPlayerFormComponent;

import static net.onixary.shapeShifterCurseFabric.ShapeShifterCurseFabric.MOD_ID;

public class BookOfShapeShifterScreen extends BaseOwoScreen<FlowLayout> {
    // player
    public PlayerEntity currentPlayer;
    // font scale
    private float textScale = 0.5f;
    // layout
    private FlowLayout leftPage;
    private FlowLayout rightPage1;
    private FlowLayout bookArea;
    private FlowLayout rightArea;
    private FlowLayout pageArea1;
    private FlowLayout pageArea2;
    private FlowLayout pageButtonContainer;
    private boolean showingPage1 = true;
    private OverlayContainer<FlowLayout> fullscreenContainer;

    private static final Identifier page1_texID = new Identifier(MOD_ID,"textures/gui/codex_page_1.png");
    private static final Identifier page2_texID = new Identifier(MOD_ID,"textures/gui/codex_page_2.png");


    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    private int getScaledWidth(int width) {
        return (int) (width / textScale);
    }

    @Override
    protected void build(FlowLayout root) {
        // UI base resolution is 320x240
        root.surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        /*root.child(
                Containers.horizontalFlow(Sizing.fixed(100), Sizing.fill(95))
                        .surface(Surface.DARK_PANEL)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .verticalAlignment(VerticalAlignment.TOP)
        );*/

        bookArea = Containers.horizontalFlow(Sizing.fixed(350), Sizing.fixed(220));
        root.child(bookArea
                .surface(Surface.tiled(page1_texID, 350, 220))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER));

        // Page Area 1
        pageArea1 = Containers.horizontalFlow(Sizing.fixed(330), Sizing.fill(100));
        bookArea.child(pageArea1
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER));

        // Left page - vertical flow with three modules
        leftPage = Containers.verticalFlow(Sizing.fixed(120), Sizing.fixed(210));
        pageArea1.child(leftPage
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5))
                .margins(Insets.of(5, 5, 2, 3)));


        // Player Doll
        leftPage.child(Components.entity(Sizing.fixed(80), currentPlayer)
                .scale(calculatePlayerScale(currentPlayer))
                .lookAtCursor(false)
                .allowMouseRotation(true)
                .transform(matrices -> {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(currentPlayer.getYaw() + 45));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-35f));
                }));

        // Title Content
        FlowLayout titleLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(50));
        leftPage.child(titleLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5)));
        Component titleDetailsButton = Components.button(
                Text.literal("+"),
                button -> showDetailScreen(CodexData.ContentType.TITLE)
        ).sizing(Sizing.fixed(10));
        titleLayout.child(titleDetailsButton.margins(Insets.of(0)));

        titleLayout.child(new ScaledLabelComponent(CodexData.getContentText(CodexData.ContentType.TITLE, currentPlayer), textScale)
                .maxWidth(getScaledWidth(100))
                .color(Color.ofRgb(0x2b2d30)));



        // Status Header
        FlowLayout statusHeaderHolder = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
        leftPage.child(statusHeaderHolder
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(1)));

        FlowLayout statusHeaderLayout = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(10));
        statusHeaderHolder.child(statusHeaderLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER));
        statusHeaderLayout.child(new ScaledLabelComponent(CodexData.headerStatus, 0.8f)
                .maxWidth(getScaledWidth(100))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .shadow(true));

        // Status Content
        FlowLayout statusLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(58));
        leftPage.child(statusLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5)));
        statusLayout.child(new ScaledLabelComponent(CodexData.getPlayerStatusText(currentPlayer), textScale)
                .maxWidth(getScaledWidth(100))
                .color(Color.ofRgb(0x2b2d30))
                );

        Component statusDetailsButton = Components.button(
                Text.literal("+"),
                button -> showDetailScreenWithText(CodexData.getPlayerStatusText(currentPlayer))
        ).sizing(Sizing.fixed(10));
        statusHeaderLayout.child(statusDetailsButton.margins(Insets.of(0)));

        // Right Area
        rightArea = Containers.verticalFlow(Sizing.fixed(190), Sizing.fixed(210));
        pageArea1.child(rightArea
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5))
                .margins(Insets.of(5, 5, 3, 2)));

        // Right page 1
        rightPage1 = Containers.verticalFlow(Sizing.fixed(180), Sizing.fill(100));
        rightArea.child(rightPage1
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.TOP));

        // Appearance Header
        FlowLayout appearanceHeaderHolder = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
        rightPage1.child(appearanceHeaderHolder
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(1)));

        FlowLayout appearanceHeaderLayout = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(10));
        appearanceHeaderHolder.child(appearanceHeaderLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER));
        appearanceHeaderLayout.child(new ScaledLabelComponent(CodexData.headerAppearance, 0.8f)
                .maxWidth(getScaledWidth(100))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .shadow(true));

        // Appearance Content
        FlowLayout appearanceLayout = Containers.verticalFlow(Sizing.fixed(182), Sizing.fixed(190));
        rightPage1.child(appearanceLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(7)));

        //appearanceLayout.child(new ScaledLabelComponent(CodexData.getDescText(CodexData.ContentType.APPEARANCE, currentPlayer), textScale)
        //        .maxWidth(getScaledWidth(170)));

        appearanceLayout.child(new ScaledLabelComponent(CodexData.getContentText(CodexData.ContentType.APPEARANCE, currentPlayer), textScale)
                .maxWidth(getScaledWidth(170))
                .color(Color.ofRgb(0x2b2d30))
                );
        // 在appearanceLayout添加按钮
        Component appearanceDetailsButton = Components.button(
                Text.literal("+"),
                button -> showDetailScreen(CodexData.ContentType.APPEARANCE)
        ).sizing(Sizing.fixed(10));
        appearanceHeaderLayout.child(appearanceDetailsButton.margins(Insets.of(0)));


        // Page Area 2
        pageArea2 = Containers.horizontalFlow(Sizing.fixed(330), Sizing.fill(100));
        bookArea.child(pageArea2
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.TOP)
                .margins(Insets.of(0)));

        // Page 2 - Pro Holder
        FlowLayout proHolder = Containers.verticalFlow(Sizing.fixed(100), Sizing.fixed(220));
        pageArea2.child(proHolder
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5))
                .margins(Insets.of(5, 5, 5, 0)));

        // Page2 - Pro Header
        FlowLayout proHeaderHolder = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
        proHolder.child(proHeaderHolder
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(1)));

        FlowLayout proHeaderLayout = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(10));
        proHeaderHolder.child(proHeaderLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER));
        proHeaderLayout.child(new ScaledLabelComponent(CodexData.headerPros, 0.8f)
                .maxWidth(getScaledWidth(100))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .shadow(true));

        // Page2 - Pro Content
        FlowLayout proLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(190));
        proHolder.child(proLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5)));
        //proLayout.child(new ScaledLabelComponent(CodexData.getDescText(CodexData.ContentType.PROS, currentPlayer), textScale)
        //        .maxWidth(getScaledWidth(80)));

        proLayout.child(new ScaledLabelComponent(CodexData.getContentText(CodexData.ContentType.PROS, currentPlayer), textScale)
                .maxWidth(getScaledWidth(80))
                .color(Color.ofRgb(0x2b2d30))
                );

        // 在proLayout添加按钮
        Component proDetailsButton = Components.button(
                Text.literal("+"),
                button -> showDetailScreen(CodexData.ContentType.PROS)
        ).sizing(Sizing.fixed(10));
        proHeaderLayout.child(proDetailsButton.margins(Insets.of(0)));

        // Page 2 - Con Holder
        FlowLayout conHolder = Containers.verticalFlow(Sizing.fixed(100), Sizing.fixed(220));
        pageArea2.child(conHolder
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5))
                .margins(Insets.of(5, 5, 0, 5)));

        // Page2 - Con Header
        FlowLayout conHeaderHolder = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
        conHolder.child(conHeaderHolder
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(1)));

        FlowLayout conHeaderLayout = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(10));
        conHeaderHolder.child(conHeaderLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER));
        conHeaderLayout.child(new ScaledLabelComponent(CodexData.headerCons, 0.8f)
                .maxWidth(getScaledWidth(100))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .shadow(true));

        // Page2 - Con Content
        FlowLayout conLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(190));
        conHolder.child(conLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5)));
        //conLayout.child(new ScaledLabelComponent(CodexData.getDescText(CodexData.ContentType.CONS, currentPlayer), textScale)
        //        .maxWidth(getScaledWidth(80)));
        conLayout.child(new ScaledLabelComponent(CodexData.getContentText(CodexData.ContentType.CONS, currentPlayer), textScale)
                .maxWidth(getScaledWidth(80))
                .color(Color.ofRgb(0x2b2d30))
                );

        // 在conLayout添加按钮
        Component conDetailsButton = Components.button(
                Text.literal("+"),
                button -> showDetailScreen(CodexData.ContentType.CONS)
        ).sizing(Sizing.fixed(10));
        conHeaderLayout.child(conDetailsButton.margins(Insets.of(0)));

        // Page 2 - Instinct Holder
        FlowLayout instinctHolder = Containers.verticalFlow(Sizing.fixed(120), Sizing.fixed(220));
        pageArea2.child(instinctHolder
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5))
                .margins(Insets.of(5, 5, 5, 5)));

        // Page2 - Instinct Header
        FlowLayout instinctHeaderHolder = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(12));
        instinctHolder.child(instinctHeaderHolder
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(1)));

        FlowLayout instinctHeaderLayout = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(10));
        instinctHeaderHolder.child(instinctHeaderLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER));
        instinctHeaderLayout.child(new ScaledLabelComponent(CodexData.headerInstincts, 0.8f)
                .maxWidth(getScaledWidth(100))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .shadow(true));

        // Page2 - Instinct Content
        FlowLayout instinctLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(140));
        instinctHolder.child(instinctLayout
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP)
                .padding(Insets.of(5)));
        instinctLayout.child(new ScaledLabelComponent(CodexData.getDescText(CodexData.ContentType.INSTINCTS, currentPlayer), textScale)
                .maxWidth(getScaledWidth(100)));
        instinctLayout.child(new ScaledLabelComponent(CodexData.getContentText(CodexData.ContentType.INSTINCTS, currentPlayer), textScale)
                .maxWidth(getScaledWidth(100))
                .color(Color.ofRgb(0x2b2d30)));

        // 在instinctLayout添加按钮
        Component instinctDetailsButton = Components.button(
                Text.literal("+"),
                button -> showDetailScreen(CodexData.ContentType.INSTINCTS)
        ).sizing(Sizing.fixed(10));
        instinctHeaderLayout.child(instinctDetailsButton.margins(Insets.of(0)));

        // A button to toggle between pages
        pageButtonContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        pageButtonContainer.child(Components.button(Text.literal(">"), b -> {
            showingPage1 = !showingPage1;
            rebuildRightPage(root);
        })
                .margins(Insets.of(0, 0, 0, 0)));

        // rebuild
        bookArea.clearChildren();
        bookArea.surface(Surface.tiled(page1_texID, 350, 220))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
        bookArea.child(pageArea1);
        bookArea.child(pageButtonContainer);
    }

    private void rebuildRightPage(FlowLayout root) {
        // Remove both pages
        bookArea.clearChildren();
        // Add the page currently showing
        if (showingPage1) {
            bookArea.surface(Surface.tiled(page1_texID, 350, 220))
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .verticalAlignment(VerticalAlignment.CENTER);
            bookArea.child(pageArea1);
        } else {
            bookArea.surface(Surface.tiled(page2_texID, 350, 220))
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .verticalAlignment(VerticalAlignment.CENTER);
            bookArea.child(pageArea2);
        }
        bookArea.child(pageButtonContainer);
    }

    private float calculatePlayerScale(PlayerEntity player) {
        float BASE_SCALE_FACTOR = 0.25f;
        PlayerFormComponent formComp = player.getComponent(RegPlayerFormComponent.PLAYER_FORM);
        //float formScale = RegFormConfig.CONFIGS.get(formComp.getCurrentForm()).getScale();
        float formScale = 1;
        return BASE_SCALE_FACTOR * (1.0f / formScale);
    }

    private void showDetailScreen(CodexData.ContentType contentType) {
        // 移除可能已存在的全屏容器
        if (fullscreenContainer != null) {
            this.uiAdapter.rootComponent.removeChild(fullscreenContainer);
        }

        FlowLayout textContainer = Containers.verticalFlow(
                Sizing.fill(95),
                Sizing.fill(95)
        );
        textContainer.padding(Insets.of(10));

        // 添加文本
        textContainer.child(
                new ScaledLabelComponent(CodexData.getContentText(contentType, currentPlayer), 0.8f)
                        .maxWidth((int) (360 / 0.8f))
                        .color(Color.WHITE)
        );

        // 创建全屏overlay
        fullscreenContainer = Containers.overlay(textContainer);
        fullscreenContainer
                .surface(Surface.flat(0xDD000000)) // 半透明黑色背景
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .zIndex(300);
        fullscreenContainer.child(textContainer);

        // 添加到根容器
        this.uiAdapter.rootComponent.child(fullscreenContainer);
    }

    private void showDetailScreenWithText(Text contentText) {
        // 移除可能已存在的全屏容器
        if (fullscreenContainer != null) {
            this.uiAdapter.rootComponent.removeChild(fullscreenContainer);
        }

        FlowLayout textContainer = Containers.verticalFlow(
                Sizing.fill(95),
                Sizing.fill(95)
        );
        textContainer.padding(Insets.of(10));

        // 添加文本
        textContainer.child(
                new ScaledLabelComponent(contentText, 0.8f)
                        .maxWidth((int) (360 / 0.8f))
                        .color(Color.WHITE)
        );

        // 创建全屏overlay
        fullscreenContainer = Containers.overlay(textContainer);
        fullscreenContainer
                .surface(Surface.flat(0xDD000000)) // 半透明黑色背景
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .zIndex(300);
        fullscreenContainer.child(textContainer);

        // 添加到根容器
        this.uiAdapter.rootComponent.child(fullscreenContainer);
    }


    @Override
    public boolean shouldPause() {
        return false; // 核心控制
    }
}
