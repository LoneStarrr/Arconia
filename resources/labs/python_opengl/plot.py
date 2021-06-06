#!/usr/bin/env python
# Generic OpenGL tutorial (uses C++) http://www.opengl-tutorial.org/
# Python-specific: http://pyopengl.sourceforge.net/context/tutorials/index.html
#
"""
Experiment with OpenGL directly from python for ease of testing
"""
import OpenGL
from OpenGL import GL
from OpenGL import GLUT
import OpenGL.GLU


def square():
    # We have to declare the points in this sequence: bottom left, bottom right, top right, top left
    GL.glBegin(GL.GL_QUADS)  # Begin the sketch
    GL.glVertex2f(100, 100, 0)  # Coordinates for the bottom left point
    GL.glVertex2f(200, 100, 0)  # Coordinates for the bottom right point
    GL.glVertex2f(200, 200, 0)  # Coordinates for the top right point
    GL.glVertex2f(100, 200, 0)  # Coordinates for the top left point

    GL.glVertex2f(100, 100, 100)  # Coordinates for the bottom left point
    GL.glVertex2f(200, 100, 100)  # Coordinates for the bottom right point
    GL.glVertex2f(200, 200, 100)  # Coordinates for the top right point
    GL.glVertex2f(100, 200, 100)  # Coordinates for the top left point

    GL.glEnd()  # Mark the end of drawing


def show_screen():
    GL.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT)
    GL.glLoadIdentity()  # Reset all graphic/shape's position
    iterate()
    GL.glColor3f(1.0, 0.0, 3.0)
    square()
    GLUT.glutSwapBuffers()


def iterate():
    GL.glViewport(0, 0, 500, 500)
    GL.glMatrixMode(GL.GL_PROJECTION)
    GL.glLoadIdentity()
    GL.glOrtho(0.0, 500, 0.0, 500, 0.0, 500.0)
    GL.glMatrixMode(GL.GL_MODELVIEW)
    GL.glLoadIdentity()


def init_opengl_gui():
    GLUT.glutInit()  # Initialize a glut instance which will allow us to customize our window
    GLUT.glutInitDisplayMode(GLUT.GLUT_RGBA | GLUT.GLUT_DEPTH | GLUT.GLUT_DOUBLE)
    GLUT.glutInitWindowSize(500, 500)   # Set the width and height of your window
    GLUT.glutInitWindowPosition(0, 0)   # Set the position at which this windows should appear
    window = GLUT.glutCreateWindow("OpenGL Experimentation")
    GLUT.glutDisplayFunc(show_screen)  # Tell OpenGL to call the show_screen method continuously
    GLUT.glutIdleFunc(show_screen)     # Draw any graphics or shapes in the show_screen function at all times
    GLUT.glutMainLoop()


init_opengl_gui()
