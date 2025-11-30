# Throw TNT Mod for Minecraft 1.21.1 (Fabric)

这个mod为Minecraft添加了一个新功能：允许玩家使用打火石投掷TNT。

## 功能描述

当玩家背包中有TNT并且手持打火石对着空气长按右键时，会投掷出一个已点燃的TNT。TNT会在2秒后爆炸。

## 使用方法

1. 确保你的物品栏中有TNT和打火石
2. 手持打火石
3. 对着空气（不要对准方块）长按右键
4. TNT会被投掷出去并在2秒后爆炸

## 构建说明

### 前置条件

- JDK 21
- Gradle

### 构建步骤

1. 克隆或下载此项目
2. 在项目根目录运行以下命令：
   ```
   ./gradlew build
   ```
3. 构建完成的mod文件位于 `build/libs/` 目录中

### 开发环境设置

1. 运行以下命令生成IDE资源：
   ```
   ./gradlew genSources
   ```
2. 根据你的IDE选择相应的命令：
   - For IntelliJ IDEA: `./gradlew idea`
   - For Eclipse: `./gradlew eclipse`

## 安装说明

1. 下载适用于Minecraft 1.21.1的[Fabric Loader](https://fabricmc.net/use/)
2. 将构建好的mod文件放入 `.minecraft/mods` 文件夹
3. 启动游戏

## 许可证

本项目采用CC0-1.0许可证发布。