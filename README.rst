meta-lxd (unofficial LXD layer for poky)
========================================

This layer provides support for building LXD and associated packages.

More information about LXD can be found at: https://github.com/lxc/lxd.

**Disclaimer - This is unofficial! I am not associated with the LXD team.** 

The goal is to provide a simple recipe to allow Poky users to add LXD support
to their images, since LXC only seems to be supported (through 
meta-virtualization) at the time of writing this document. 

The testing so far has been limited to basic sanity checks of LXD
functionality in a QEMU environment, your mileage may vary. If you
encounter any problem, please submit an issue in this project.

Poky support is currently limited to the *gatesgarth* version, but
this may evolve with future developments.

Dependencies
------------

This layer depends on the `meta-virtualization` and `meta-openembedded` layers.

  URI: https://git.yoctoproject.org/cgit/cgit.cgi/meta-virtualization/
  branch: gatesgarth

  URI: https://git.congatec.com/yocto/meta-openembedded
  branch: gatesgarth

Maintenance
-----------

Issue reports and merge requests are welcome!

Maintainer: Florian Curinga <florian.curinga@gmail.com>

Getting started
---------------

You may start by adding the layer to your `bblayers.conf` file along with
its dependencies:

.. code-block:: 

  BBLAYERS ?= " \
    [...] \
    /path/to/meta-openembedded/meta-networking \
    /path/to/meta-virtualization \
    /path/to/meta-lxd \
    [...] "


This will enable you to build the `lxd` recipe as well as an example image
`core-image-sato-lxd` that corresponds to the standard `core-image-sato` with
the `lxd` package added.

After installing the package, make sure to read the great LXD "Getting started 
guide": https://linuxcontainers.org/lxd/getting-started-cli/.

Adding the lxd package to your image
************************************

This can simply be achieved by adding the package to your image:

.. code-block::

  IMAGE_INSTALL += " lxd "


Building the example image
**************************

You may use `bitbake` to build this image:

.. code-block::

  bitbake core-image-sato-lxd


Runtime setup
*************

The LXD daemon does not start automatically, so you first need to start it:

.. code-block:: shell
  
  sudo -E PATH=$PATH LD_LIBRARY_PATH=$LD_LIBRARY_PATH /usr/bin/lxd --group sudo &


From there, you can setup LXD as you would normally do:

.. code-block:: shell

  lxd init
