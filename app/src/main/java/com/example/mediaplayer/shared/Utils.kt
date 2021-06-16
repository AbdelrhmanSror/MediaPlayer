/*
 * Copyright 2019 Abdelrhman Sror. All rights reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.example.mediaplayer.shared

import android.os.Build


fun isNougatOrLater(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
}

fun isOreoOrLater(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}

fun isQOrLater(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

}


fun <K, V> HashMap<K, V>.update(updatedPlayerState: HashMap<K, V>) {
    val deletedItems = arrayListOf<K>()
    when {
        updatedPlayerState.size > this.size -> {
            updatedPlayerState.filter {
                if (!(this.contains(it.key))) {
                    this[it.key] = it.value

                }
                true
            }
        }
        updatedPlayerState.size < this.size -> {
            this.filter {
                if (!(updatedPlayerState.contains(it.key))) {
                    deletedItems.add(it.key)
                }
                true
            }
            for (i in 0 until deletedItems.size) {
                this.remove(deletedItems[i])

            }

        }
    }

}
