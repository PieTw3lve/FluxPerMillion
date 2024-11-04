package com.github.pietw3lve.fpm.handlers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.pietw3lve.fpm.FluxPerMillion;

public class TreeHandler {
    
    private final FluxPerMillion plugin;
    private final Set<Material> treeLogs;
    private final Set<Material> strippedTreeLogs;
    private final Set<Material> treeLeaves;
    private final int MAX_TREE_LENGTH = 500;
    private final int NATURAL_LEAVES_THRESHOLD = 5;

    /**
     * TreeUtil Constructor.
     * @param plugin
     */
    public TreeHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.treeLogs = new HashSet<>(Arrays.asList(
                Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG,
                Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
                Material.MANGROVE_LOG, Material.CHERRY_LOG

        ));
        this.strippedTreeLogs = new HashSet<>(Arrays.asList(
                Material.STRIPPED_OAK_LOG, Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_SPRUCE_LOG,
                Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
                Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG
        ));
        this.treeLeaves = new HashSet<>(Arrays.asList(
                Material.OAK_LEAVES, Material.BIRCH_LEAVES, Material.SPRUCE_LEAVES,
                Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
                Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES
        ));
    }

    /**
     * Returns a set of all connected logs and leaves in the tree.
     * @param block The block to start the tree search from.
     * @return A set of all connected logs and leaves in the tree 
     * or an empty set if the tree is not valid.
     */
    public Set<Block> getLiveTree(Block block) {
        Set<Block> treeBlocks = new HashSet<>();
        traverseTree(block, treeBlocks);

        long naturalLeaves = treeBlocks.stream()
            .filter(b -> treeLeaves.contains(b.getType()))
            .map(b -> (Leaves) b.getBlockData())
            .filter(leaves -> !leaves.isPersistent())
            .count();

        if (naturalLeaves <= NATURAL_LEAVES_THRESHOLD) {
            treeBlocks.clear();
        }

        return treeBlocks;
    }

    /**
     * Traverse the tree to find all connected logs and leaves
     * and add them to the treeBlocks set.
     * @param startBlock The block to start the tree search from.
     * @param treeBlocks An empty set to store the tree blocks.
     */
    private void traverseTree(Block startBlock, Set<Block> treeBlocks) {
        Queue<Block> queue = new LinkedList<>();
        queue.add(startBlock);

        BlockFace[] directions = BlockFace.values();

        while (!queue.isEmpty() && treeBlocks.size() < MAX_TREE_LENGTH) {
            Block block = queue.poll();
            if (treeBlocks.contains(block)) {
                continue;
            }

            treeBlocks.add(block);

            for (BlockFace direction : directions) {
                Block adjacentBlock = block.getRelative(direction);
                if (treeLogs.contains(adjacentBlock.getType()) || strippedTreeLogs.contains(adjacentBlock.getType())) {
                    queue.add(adjacentBlock);
                } else if (treeLeaves.contains(adjacentBlock.getType())) {
                    Leaves leaves = (Leaves) adjacentBlock.getBlockData();
                    if (!leaves.isPersistent()) {
                        queue.add(adjacentBlock);
                    }
                }

                // Check for diagonal blocks
                for (BlockFace diagonalDirection : directions) {
                    if (diagonalDirection != direction) {
                        Block diagonalBlock = adjacentBlock.getRelative(diagonalDirection);
                        if (treeLogs.contains(diagonalBlock.getType()) || strippedTreeLogs.contains(diagonalBlock.getType())) {
                            queue.add(diagonalBlock);
                        } else if (treeLeaves.contains(diagonalBlock.getType())) {
                            Leaves leaves = (Leaves) diagonalBlock.getBlockData();
                            if (!leaves.isPersistent()) {
                                queue.add(diagonalBlock);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the number of logs in the tree.
     * @param tree The set of blocks in the tree.
     * @return The number of logs in the tree.
     */
    public int getTreeLogsCount(Set<Block> tree) {
        int count = 0;
        for (Block block : tree) {
            if (isTreeLog(block) || isStrippedTreeLog(block)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Breaks the tree by breaking all blocks in the tree set.
     * @param tree The set of blocks in the tree.
     */
    public void breakTree(Set<Block> tree) {
        for (Block block : tree) {
            block.removeMetadata("fpm:tree_dead", plugin);
            block.setType(Material.AIR);
        }
    }

    /**
     * Sets the "fpm:tree_dead" metadata on all blocks in the tree.
     * @param tree The set of blocks in the tree.
     */
    public void markTreeAsDead(Set<Block> tree) {
        for (Block block : tree) {
            block.setMetadata("fpm:tree_dead", new FixedMetadataValue(plugin, true));
        }
    }

    /**
     * Checks if the block is a tree block.
     * @param block The block to check
     * @return True if the block is a tree block, false otherwise.
     */
    public boolean isTreeBlock(Block block) {
        return treeLogs.contains(block.getType()) || treeLeaves.contains(block.getType());
    }

    /**
     * Checks if the block is a tree leaf.
     * @param block The block to check
     * @return True if the block is a tree leaf, false otherwise.
     */
    public boolean isTreeLeaf(Block block) {
        return treeLeaves.contains(block.getType());
    }

    /**
     * Check if the block is a tree log.
     * @param block The block to check.
     * @return True if the block is a tree log, false otherwise.
     */
    public boolean isTreeLog(Block block) {
        return treeLogs.contains(block.getType());
    }

    /**
     * Check if the block is a stripped tree log.
     * @param block The block to check.
     * @return True if the block is a stripped tree log, false otherwise.
     */
    public boolean isStrippedTreeLog(Block block) {
        return strippedTreeLogs.contains(block.getType());
    }

    /**
     * Returns the stripped tree logs set.
     * @return A set of stripped tree log materials.
     */
    public Set<Material> getStrippedTreeLogs() {
        return strippedTreeLogs;
    }

    /**
     * Returns the tree logs set.
     * @return A set of tree log materials.
     */
    public Set<Material> getTreeLogs() {
        return treeLogs;
    }

    /**
     * Returns the tree leaves set.
     * @return A set of tree leaf materials.
     */
    public Set<Material> getTreeLeaves() {
        return treeLeaves;
    }
}
