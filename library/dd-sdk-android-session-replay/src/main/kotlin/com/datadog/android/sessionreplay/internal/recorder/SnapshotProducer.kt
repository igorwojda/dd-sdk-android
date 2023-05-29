/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.sessionreplay.internal.recorder

import android.view.View
import android.view.ViewGroup
import com.datadog.android.sessionreplay.model.MobileSegment
import java.util.LinkedList

internal class SnapshotProducer(
    private val treeViewTraversal: TreeViewTraversal,
    private val optionSelectorDetector: OptionSelectorDetector =
        ComposedOptionSelectorDetector(listOf(DefaultOptionSelectorDetector()))
) {

    fun produce(
        rootView: View,
        systemInformation: SystemInformation,
        delayedCallbackInfo: DelayedCallbackInfo,
    ): Node? {
        return convertViewToNode(
                rootView,
                MappingContext(systemInformation),
                LinkedList(),
                delayedCallbackInfo,
        )
    }

    @Suppress("ComplexMethod", "ReturnCount")
    private fun convertViewToNode(
        view: View,
        mappingContext: MappingContext,
        parents: LinkedList<MobileSegment.Wireframe>,
        delayedCallbackInfo: DelayedCallbackInfo,
    ): Node? {

        val currentNode = Node(
                children = arrayListOf(),
                wireframes = arrayListOf(),
                parents = parents
        )

        if (delayedCallbackInfo.root == null) {
            delayedCallbackInfo.root = currentNode
        }

        val traversedTreeView = treeViewTraversal.traverse(
                view,
                mappingContext,
                delayedCallbackInfo,
        )
        val nextTraversalStrategy = traversedTreeView.nextActionStrategy
        val resolvedWireframes = traversedTreeView.mappedWireframes
        if (nextTraversalStrategy == TreeViewTraversal.TraversalStrategy.STOP_AND_DROP_NODE) {
            return null
        }
        if (nextTraversalStrategy == TreeViewTraversal.TraversalStrategy.STOP_AND_RETURN_NODE) {
            return Node(wireframes = resolvedWireframes, parents = parents)
        }

        val childNodes = LinkedList<Node>()

        if (view is ViewGroup &&
            view.childCount > 0 &&
            nextTraversalStrategy == TreeViewTraversal.TraversalStrategy.TRAVERSE_ALL_CHILDREN
        ) {
            val childMappingContext = resolveChildMappingContext(view, mappingContext)
            val parentsCopy = LinkedList(parents).apply { addAll(resolvedWireframes) }
            for (childIdx in 0 until view.childCount) {
                val viewChild = view.getChildAt(childIdx) ?: continue
                delayedCallbackInfo.current = currentNode

                convertViewToNode(
                        viewChild,
                        childMappingContext,
                        parentsCopy,
                        delayedCallbackInfo,
                )?.let {
                    childNodes.add(it)
                }
            }
        }

        currentNode.children = childNodes
        currentNode.wireframes = resolvedWireframes

        return currentNode
    }

    private fun resolveChildMappingContext(
        parent: ViewGroup,
        parentMappingContext: MappingContext
    ): MappingContext {
        return if (optionSelectorDetector.isOptionSelector(parent)) {
            parentMappingContext.copy(hasOptionSelectorParent = true)
        } else {
            parentMappingContext
        }
    }
}