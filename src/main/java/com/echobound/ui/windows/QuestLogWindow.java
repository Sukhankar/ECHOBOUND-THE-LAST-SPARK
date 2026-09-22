package com.echobound.ui.windows;

import com.echobound.core.UnifiedGameContext;
import com.echobound.items.ItemDefinition;
import com.echobound.items.ItemRegistry;
import com.echobound.quest.QuestDefinition;
import com.echobound.quest.QuestStatus;
import com.echobound.quest.QuestTier;
import com.echobound.story.StoryChapter;
import com.echobound.story.SubChapter;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class QuestLogWindow {
    private final QuestTier[] tiers = QuestTier.values();
    private int currentTierIndex = 0;
    private int questCursor = 0;

    public void handleKeyPress(int keyCode, UnifiedGameContext ctx) {
        List<QuestDefinition> quests = getQuestsForCurrentTier(ctx);

        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            currentTierIndex = (currentTierIndex - 1 + tiers.length) % tiers.length;
            questCursor = 0;
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            currentTierIndex = (currentTierIndex + 1) % tiers.length;
            questCursor = 0;
        } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            if (!quests.isEmpty()) {
                questCursor = (questCursor - 1 + quests.size()) % quests.size();
            }
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            if (!quests.isEmpty()) {
                questCursor = (questCursor + 1) % quests.size();
            }
        } else if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
            QuestDefinition q = getSelectedQuest(ctx);
            if (q != null && (q.status == QuestStatus.AVAILABLE || q.status == QuestStatus.LOCKED)) {
                ctx.questManager.startQuest(q.id);
            }
        }
    }

    public List<QuestDefinition> getQuestsForCurrentTier(UnifiedGameContext ctx) {
        return ctx.questManager.getQuestsByTier(tiers[currentTierIndex]);
    }

    public QuestDefinition getSelectedQuest(UnifiedGameContext ctx) {
        List<QuestDefinition> list = getQuestsForCurrentTier(ctx);
        if (questCursor >= 0 && questCursor < list.size()) {
            return list.get(questCursor);
        }
        return null;
    }

    public void render(Graphics2D g, UnifiedGameContext ctx, int width, int height) {
        // Overlay backdrop
        g.setColor(new Color(14, 18, 28, 240));
        int winW = 280;
        int winH = 160;
        int startX = (width - winW) / 2;
        int startY = (height - winH) / 2;

        g.fillRoundRect(startX, startY, winW, winH, 8, 8);
        g.setColor(new Color(130, 90, 240));
        g.drawRoundRect(startX, startY, winW, winH, 8, 8);

        // Header Title
        g.setFont(new Font("Monospaced", Font.BOLD, 9));
        g.setColor(new Color(255, 215, 0));
        g.drawString("JOURNAL & QUEST CHRONICLES", startX + 10, startY + 12);

        // Tier Tabs (Left / Right)
        String tierName = tiers[currentTierIndex].name().replace('_', ' ');
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(200, 160, 255));
        g.drawString("< [" + (currentTierIndex + 1) + "/" + tiers.length + "] " + tierName + " >", startX + 160, startY + 12);

        // Left Panel: Quest List
        int listStartX = startX + 10;
        int listStartY = startY + 22;
        int listW = 120;
        int listH = 92;

        g.setColor(new Color(18, 22, 34));
        g.fillRect(listStartX, listStartY, listW, listH);
        g.setColor(new Color(50, 60, 85));
        g.drawRect(listStartX, listStartY, listW, listH);

        List<QuestDefinition> quests = getQuestsForCurrentTier(ctx);
        if (quests.isEmpty()) {
            g.setFont(new Font("Monospaced", Font.ITALIC, 7));
            g.setColor(Color.GRAY);
            g.drawString("No quests discovered.", listStartX + 6, listStartY + 20);
        } else {
            for (int i = 0; i < quests.size(); i++) {
                QuestDefinition q = quests.get(i);
                int qy = listStartY + 4 + i * 14;
                boolean isCursor = (i == questCursor);

                if (isCursor) {
                    g.setColor(new Color(60, 45, 110));
                    g.fillRect(listStartX + 2, qy - 1, listW - 4, 13);
                }

                g.setFont(new Font("Monospaced", isCursor ? Font.BOLD : Font.PLAIN, 7));
                Color statusColor;
                if (q.status == QuestStatus.COMPLETED) statusColor = new Color(80, 255, 120);
                else if (q.status == QuestStatus.IN_PROGRESS) statusColor = new Color(255, 210, 60);
                else statusColor = Color.LIGHT_GRAY;

                g.setColor(statusColor);
                String label = (isCursor ? "> " : "  ") + q.title;
                if (label.length() > 16) label = label.substring(0, 16);
                g.drawString(label, listStartX + 4, qy + 9);
            }
        }

        // Right Panel: Quest Details Inspector
        int inspStartX = startX + 138;
        int inspStartY = startY + 22;
        int inspW = 132;
        int inspH = 92;

        g.setColor(new Color(18, 22, 34));
        g.fillRect(inspStartX, inspStartY, inspW, inspH);
        g.setColor(new Color(50, 60, 85));
        g.drawRect(inspStartX, inspStartY, inspW, inspH);

        QuestDefinition sel = getSelectedQuest(ctx);
        if (sel != null) {
            g.setFont(new Font("Monospaced", Font.BOLD, 8));
            g.setColor(new Color(255, 225, 100));
            g.drawString(sel.title, inspStartX + 6, inspStartY + 12);

            g.setFont(new Font("Monospaced", Font.PLAIN, 6));
            g.setColor(new Color(190, 205, 230));
            // Wrap or abbreviate description
            String desc = sel.description;
            if (desc.length() > 32) {
                g.drawString(desc.substring(0, Math.min(32, desc.length())), inspStartX + 6, inspStartY + 24);
                if (desc.length() > 32) {
                    g.drawString(desc.substring(32, Math.min(64, desc.length())), inspStartX + 6, inspStartY + 33);
                }
            } else {
                g.drawString(desc, inspStartX + 6, inspStartY + 24);
            }

            // Progress bar
            g.setFont(new Font("Monospaced", Font.BOLD, 7));
            g.setColor(new Color(255, 180, 60));
            g.drawString("PROGRESS: [" + sel.currentProgress + "/" + sel.targetProgress + "]", inspStartX + 6, inspStartY + 48);

            // Faction & Reward
            g.setFont(new Font("Monospaced", Font.PLAIN, 7));
            g.setColor(new Color(140, 220, 255));
            g.drawString("Faction: " + sel.faction.displayName, inspStartX + 6, inspStartY + 60);

            ItemDefinition rewDef = ItemRegistry.get(sel.rewardItemId);
            g.drawString("Reward: " + sel.rewardItemCount + "x " + (rewDef != null ? rewDef.name : "Item") + " +" + sel.reputationReward + " Rep",
                    inspStartX + 6, inspStartY + 72);

            // Status Badge
            g.setColor(sel.status == QuestStatus.COMPLETED ? new Color(50, 160, 70) : new Color(130, 90, 30));
            g.fillRect(inspStartX + 6, inspStartY + 77, 60, 11);
            g.setFont(new Font("Monospaced", Font.BOLD, 6));
            g.setColor(Color.WHITE);
            g.drawString(sel.status.name(), inspStartX + 10, inspStartY + 85);
        }

        // Bottom Story Chapters Progress Bar
        int botY = startY + 118;
        g.setColor(new Color(22, 28, 42));
        g.fillRect(startX + 8, botY, winW - 16, 36);
        g.setColor(new Color(60, 75, 105));
        g.drawRect(startX + 8, botY, winW - 16, 36);

        int globalSub = ctx.storyEngine.getCurrentSubChapterIndex();
        StoryChapter chapter = ctx.storyEngine.getCurrentMajorChapter();
        SubChapter sub = ctx.storyEngine.getSubChapter(globalSub);

        g.setFont(new Font("Monospaced", Font.BOLD, 7));
        g.setColor(new Color(255, 215, 0));
        g.drawString("STORY: Chapter " + (chapter != null ? chapter.chapterNumber + " - " + chapter.title : "1"), startX + 12, botY + 10);

        g.setFont(new Font("Monospaced", Font.PLAIN, 7));
        g.setColor(new Color(180, 220, 255));
        g.drawString("Subchapter " + globalSub + "/500: " + (sub != null ? sub.title : ""), startX + 12, botY + 20);

        // Progress bar (globalSub / 500)
        int barW = winW - 32;
        int barH = 5;
        g.setColor(new Color(10, 15, 25));
        g.fillRect(startX + 12, botY + 25, barW, barH);
        float pct = Math.min(1.0f, (float) globalSub / 500.0f);
        g.setColor(new Color(0, 220, 255));
        g.fillRect(startX + 12, botY + 25, (int) (barW * pct), barH);
        g.setColor(new Color(50, 90, 130));
        g.drawRect(startX + 12, botY + 25, barW, barH);
    }

    public int getCurrentTierIndex() {
        return currentTierIndex;
    }

    public void setCurrentTierIndex(int idx) {
        this.currentTierIndex = idx % tiers.length;
    }

    public int getQuestCursor() {
        return questCursor;
    }

    public void setQuestCursor(int cursor) {
        this.questCursor = cursor;
    }
}
