# -*- coding: utf-8 -*-
from setuptools import setup, find_packages

try:
    long_description = open("README.rst").read()
except IOError:
    long_description = ""

setup(
    name="kvdnc",

    version="1.6.8_1",
    description="kvdn client library and tool",
    license="Apache",
    author="Grant Haywood",
    packages=["kvdn_client"],
    install_requires=["requests"],
    long_description=long_description,
      entry_points={
          'console_scripts': [
              'kvdn-cli = kvdn_client.__main__:main'
          ]
      },
    classifiers=[
        "Programming Language :: Python",
        "Programming Language :: Python :: 2.7",
    ]
)
