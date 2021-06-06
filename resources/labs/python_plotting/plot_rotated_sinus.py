#!/usr/bin/env python
"""
Fun With Vectors
"""
# https://stackoverflow.com/questions/47319238/python-plot-3d-vectors
# pylint: disable=invalid-name

import math
import numpy as np
import plotly.graph_objs as go


class PlotStuff:
    def __init__(self, layout=None):
        self.data = []
        self.layout = layout or go.Layout(margin=dict(l=4, r=4, b=4, t=4))

    def plot(self):
        fig = go.Figure(data=self.data, layout=self.layout)
        fig.update_scenes(xaxis_autorange="reversed", yaxis_autorange="reversed")
        fig.show()

    def add_vector(self, v, name=None, origin=None):
        """
        Add a vector to plot
        """
        if not origin:
            origin = (0, 0, 0)

        # coord = np.sum([origin, v], axis=0)
        coord = v

        vector = go.Scatter3d(
            x=[origin[0], coord[0]],
            y=[origin[1], coord[1]],
            z=[origin[2], coord[2]],
            marker=dict(
                size=[0, 5],
                color=['blue'],
                line=dict(width=5, color='DarkSlateGrey')
            ),
            name=name
        )
        self.data.append(vector)
        return self


def add_sine_wave(plotter, num_points=40, width=10, amplitude=2.5, z=0):
    prev = (0, 0, 0)
    for i in range(num_points):
        x = i / float(num_points) * width
        y = math.sin(i / float(num_points) * 2 * math.pi)
        v = (x, y, z)
        if i != 0:
            plotter.add_vector(v, origin=prev)
        prev = v


def main():
    """
    """
    plotter = PlotStuff()
    add_sine_wave(plotter)
    plotter.plot()


main()
