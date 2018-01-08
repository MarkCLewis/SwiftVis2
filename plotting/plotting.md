Plots in SwiftVis2 are grids of plots with certain shared axes. Each cell in the grid can have
multiple plot styles in it, each associated with an x and a y axis. The axes can be drawn on the
high side, or the low side of the plot so that each row and each column can have two axes associated with it.
The relative heights of rows and widths of columns can also be adjusted.

Most of the time, you will be able to get the results that you want using the helper methods in the
`Plot` object. This document describes the more general approach where you build a `Plot` specifying
the grid of plot styles and axes yourself.