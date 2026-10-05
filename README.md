# 恶魔契约 / Devil Contract

为 **Minecraft 1.21.1**（**NeoForge 21.1+**）添加《高校DXD》赤龙帝的笼手与相关内容的模组。

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 简介

添加武器 **赤龙帝的笼手 (Boosted Gear)**、**绿球 (Green Ball)**，以及 **圣狱维度**、**空间领域** 等玩法内容。
笼手拥有 4.5 格攻击距离、可叠加的强化层数、贯穿式矛突刺，并可切换多种工具形态。

## 依赖

| 模组 | 类型 | 说明 |
| --- | --- | --- |
| Minecraft | 必需 | `1.21.1` |
| NeoForge | 必需 | `21.1+` |
| [Patchouli](https://modrinth.com/mod/patchouli) | 可选 | 不安装则首次登录不会获得合成指南书 |

## 安装

1. 安装 Minecraft `1.21.1` 与 [NeoForge](https://neoforged.net/) `21.1+`
2. 把 `raymovies-1.0.0.jar` 放进 `.minecraft/mods/`
3. 启动游戏

从 [CurseForge](https://www.curseforge.com/minecraft/mc-mods) 或 [Modrinth](https://modrinth.com/mod) 下载也可以。

## 玩法说明

### 赤龙帝的笼手 (Boosted Gear)

合成方式：

```
红 绿 红      R = 红石块
红 星 红      E = 绿宝石块
红 金 红      S = 下界之星
              G = 金块
```

| 操作 | 效果 |
| --- | --- |
| 左键 | 普通攻击，**4.5 格**距离 |
| **V 键** | 叠加一层强化：每层 **+4 伤害**，最高 **10 层**。攻击命中会消耗一层 |
| **右键** | **矛突刺**：贯穿视线直线上的所有敌人并**向前冲刺约 9.6 格**，冷却 **1 秒** |
| **G 键** | 打开径向菜单 |
| **中键** | 传送到 / 离开 **圣狱维度** |

所有按键都可在 **选项 → 按键设置 → 血怒契约** 中自定义。

### 强化

每次按 V 键叠加强化，获得：

- 攻击伤害 **+4 × 层数**
- 攻击距离 **+3 格**
- 移动速度 **+40%**
- 火焰免疫

冷却随等级缩短：基础 10 秒，等级 ≥10 降至 4 秒，之后每 5 级再 -0.5 秒（最低 0.5 秒）。

**攻击会让强化清零**，所以要用光层数打出伤害。

### 装备模式（径向菜单内）

| 能力 | 效果 |
| --- | --- |
| 飞行 | 双击跳跃起飞，等级越高持续越久 |
| 祝福 | 等级 ≥10 时免疫火焰并提升移动速度 |
| 空间领域 | 范围内**所有单位（自己除外）每 2 秒受到 0.5 点伤害**，半径与时长随等级提升 |

### 绿球 (Green Ball)

按 V 键叠加强化，最多 5 层，每层 **+3 攻击**。攻击命中消耗一层。

### 其他

- **商人**：可用绿宝石交易钻石与金锭
- **国际象棋 - 兵**：僵尸被玩家击杀时有 1% 概率掉落
- **首次登录**：自动传送到最近的平原村庄钟旁，并生成一棵巨型樱花树

## 从源码构建

```bash
git clone https://github.com/llhhvhh/raymovies.git
cd raymovies
./gradlew build
```

产物位于 `build/libs/raymovies-1.0.0.jar`。

需要 JDK 21。开发者请参考 [NeoForged 文档](https://docs.neoforged.net/)。

## 许可

[MIT](LICENSE) · 移植自 [NeoForged MDK](https://github.com/NeoForged/MDK)

Minecraft 相关代码使用 Mojang 官方映射名称，许可见 [Mojang.md](https://github.com/NeoForged/NeoForm/blob/main/Mojang.md)。
