/*
 * This script is executed inside the UI thread, so be sure to  call 
 * long running code in another thread.
 *
 * You have the following options
 * - execOutsideUI { // your code }
 * - execFuture { // your code }
 * - Thread.start { // your code }
 *
 * You have the following options to run code again inside the UI thread
 * - execInsideUIAsync { // your code }
 * - execInsideUISync { // your code }
 */

import com.ventplan.desktop.VentplanSplash
import groovy.swing.SwingBuilder

SwingBuilder.lookAndFeel('nimbus', 'gtk', ['metal', [boldFonts: false]])
VentplanSplash.instance.setup()
VentplanSplash.instance.initializing()
