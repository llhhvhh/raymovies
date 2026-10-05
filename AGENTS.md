# 项目进度摘要 — 恶魔契约 (Boosted Gear) Mod

## 目标
为 NeoForge 1.21.1 构建一个添加 Boosted Gear 武器（High School DxD）的 Mod，支持叠加强化、冷却、按键绑定、护手物品栏槽位和右臂渲染。

## 模组信息
- Mod ID: `raymovies`, group: `com.yourname.dxdweapons`, 名称: "恶魔契约"
- JAR: `build/libs/raymovies-1.0.0.jar` → 部署至 `NeoForge_1.21.1_dev/mods/`
- NeoForge: `21.1.228` (dev), `21.1.234` (runtime)
- Java 21.0.7 (Microsoft), Gradle 9.2.1

## 已完成

### 核心功能
- `BoostedGearItem` 继承 `SwordItem` (下界合金品质) — 副手/护手槽触发 Boost，每层 +4 伤害，最高 10 层
- Boost 效果 "赤龙帝的强化" — 每次攻击消耗一层
- 每次 Boost 给予 Speed II (10s)、Fire Resistance I (10s)、+3 方块交互距离 (10s)
- 冷却基于玩家经验等级：基础 10s，等级 ≥10 减至 4s，每 5 级再 -0.5s，最低 0.5s (per-item-stack)
- 30% 点燃目标概率
- **GeckoLib 已移除** — 加载任意 `geo.json` 会导致卡死，改用 vanilla 2D 模型
- **GauntletSlotHandler** 在库存 GUI `(75, 42)` 渲染槽位，点击直接传递物品到/从玩家背包

### 网络与按键
- R 键绑定 (`ModKeyBindings`, `ClientTickEvent.Pre`)
- `BoostPacket` — 服务端检查副手 + 护手槽验证
- `GauntletSlotPacket` (client→server) — 存储/移除护手槽物品
- `GauntletSyncPacket` (server→client) — 同步 `GAUNTLET_SLOT` attachment 数据
- `ModMessages` — 注册所有三个包
- `ServerEvents` — 登录时同步护手槽数据

### 渲染
- `GauntletRenderLayer` — 玩家右臂 (`body.translate(0.35, 0.7, 0.0)`) 渲染护手槽物品
- 通过 `modEventBus.addListener(GauntletRenderLayer::registerAll)` 注册 (ModEventBus)
- 通过 `EntityRenderersEvent.AddLayers` 注入所有玩家皮肤

### 效果与声音
- `ModEffects.BOOSTED` — `ADD_VALUE` 4.0 于 `ATTACK_DAMAGE`
- `ModSounds` (`boosted_gear.boost` SoundEvent)
- `sounds.json` — 备用三叉戟雷声
- 语言文件：`en_us.json` + `zh_cn.json`

### 资源
- 绿色宝石 2D 物品纹理 (`textures/item/boosted_gear.png`) — 程序生成 (C# .NET)
- 基础 3D 模型 (`geo/item/boosted_gear.geo.json`) + 待机动画 — 占位符，因 GeckoLib 卡死未使用

## 修正的 Bug
- **修复启动崩溃** (2025-06-28): `EntityRenderersEvent.AddLayers` 是 `IModBusEvent`，不能在 `NeoForge.EVENT_BUS` 上注册。将 `onAddLayers` 从 `ClientEvents` 移至 `modEventBus.addListener(GauntletRenderLayer::registerAll)`。注册模式：
  - ModEventBus: `EntityRenderersEvent.AddLayers` (IModBusEvent)
  - GameBus (NeoForge.EVENT_BUS): `ClientEvents`, `GauntletSlotHandler`, `ModEvents`, `ServerEvents`

## 关键文件
- `src/main/java/com/yourname/dxdweapons/DxDRayMod.java` — 事件总线注册 (line 60: modEventBus for AddLayers)
- `src/main/java/com/yourname/dxdweapons/item/BoostedGearItem.java`
- `src/main/java/com/yourname/dxdweapons/item/ModEvents.java` — 攻击移除强化
- `src/main/java/com/yourname/dxdweapons/effect/ModEffects.java` + `BoostedEffect.java`
- `src/main/java/com/yourname/dxdweapons/client/ClientEvents.java` — R 键 + 声效 (GameBus)
- `src/main/java/com/yourname/dxdweapons/client/GauntletRenderLayer.java` — 右臂渲染
- `src/main/java/com/yourname/dxdweapons/client/GauntletSlotHandler.java` — 库存 UI 槽位
- `src/main/java/com/yourname/dxdweapons/client/ModKeyBindings.java`
- `src/main/java/com/yourname/dxdweapons/sound/ModSounds.java`
- `src/main/java/com/yourname/dxdweapons/attachment/ModAttachments.java` — GAUNTLET_SLOT
- `src/main/java/com/yourname/dxdweapons/network/` — BoostPacket, GauntletSlotPacket, GauntletSyncPacket, ModMessages
- `src/main/java/com/yourname/dxdweapons/event/ServerEvents.java`

## 后续步骤
1. 测试游戏是否能启动 (应用补丁后)
2. 用户提供自定义 Boost 音效 OGG (`assets/raymovies/sounds/Boost.ogg`)
3. 测试完整功能：护手槽 UI、R 键强化、攻击移除、右臂渲染
