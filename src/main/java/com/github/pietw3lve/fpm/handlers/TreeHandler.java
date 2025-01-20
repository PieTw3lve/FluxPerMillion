package com.github.pietw3lve.fpm.handlers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.metadata.FixedMetadataValue;

import com.github.pietw3lve.fpm.FluxPerMillion;

public class TreeHandler {
    
    private final FluxPerMillion plugin;
    private final Set<Material> logs;
    private final Set<Material> leaves;
    private final int MAX_TREE_LENGTH = 500;
    private final int NATURAL_LEAVES_THRESHOLD = 5;

    /**
     * TreeUtil Constructor.
     * @param plugin
     */
    public TreeHandler(FluxPerMillion plugin) {
        this.plugin = plugin;
        this.logs = Tag.LOGS_THAT_BURN.getValues();
        this.leaves = Tag.LEAVES.getValues();
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
            .filter(b -> leaves.contains(b.getType()))
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
                if (logs.contains(adjacentBlock.getType())) {
                    queue.add(adjacentBlock);
                } else if (leaves.contains(adjacentBlock.getType())) {
                    Leaves leaves = (Leaves) adjacentBlock.getBlockData();
                    if (!leaves.isPersistent()) {
                        queue.add(adjacentBlock);
                    }
                }

                // Check for diagonal blocks
                for (BlockFace diagonalDirection : directions) {
                    if (diagonalDirection != direction) {
                        Block diagonalBlock = adjacentBlock.getRelative(diagonalDirection);
                        if (logs.contains(diagonalBlock.getType())) {
                            queue.add(diagonalBlock);
                        } else if (leaves.contains(diagonalBlock.getType())) {
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
            if (isTreeBlock(block)) {
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
        return logs.contains(block.getType());
    }

    /**
     * Checks if the block is a tree leaf.
     * @param block The block to check
     * @return True if the block is a tree leaf, false otherwise.
     */
    public boolean isTreeLeaf(Block block) {
        return leaves.contains(block.getType());
    }

    /**
     * Returns the tree logs set.
     * @return A set of tree log materials.
     */
    public Set<Material> getTreeLogs() {
        return logs;
    }

    /**
     * Returns the tree leaves set.
     * @return A set of tree leaf materials.
     */
    public Set<Material> getTreeLeaves() {
        return leaves;
    }
}
