#
# LXD recipe (unofficial!)
# Simple adaptation of the build steps described at: https://github.com/lxc/lxd
#
DESCRIPTION = "LXD is a next generation system container manager. It offers a user experience similar to virtual machines but using Linux containers instead."
SECTION = "admin"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# Recipe dependencies
DEPENDS=" lxc sqlite3 pkgconfig libuv udev libcap acl go-native apparmor "
RDEPENDS_lxd=" squashfs-tools lxc lxc-networking nftables ebtables systemd "

inherit go
inherit pkgconfig
inherit autotools

# Release to be built, see https://github.com/lxc/lxd/releases
LXD_RELEASE_VERSION="4.13"

# Source definition
LXD_PKG_NAME="lxd-${LXD_RELEASE_VERSION}"
SRC_URI = "     https://github.com/lxc/lxd/releases/download/${LXD_PKG_NAME}/${LXD_PKG_NAME}.tar.gz \
                file://0001-enable-cross-compile-raft-dqlite.patch  "
SRC_URI[sha256sum] = "8efd95ad4023e0d197490deb169512977ce81e3560cfd5dd93511ae438405970"

# TODO: check if this is correct
FILES_${PN} += "${libdir}/*"

# We need CGO
CGO_ENABLED = "1"

# Required for some libraries
DEBUG_BUILD = "1"
DEBUG_FLAGS="-O1"

do_compile() {
    #
    # Implements build steps from 
    # https://github.com/lxc/lxd#from-source-building-a-release
    #
    cd "../${LXD_PKG_NAME}/"

    # Detect toolchain prefix (ex: 'x86_64-poky-linux') from CC (ex: 
    # 'x86_64-poky-linux-gcc ...')
    # This is necessary to enable the cross-compilation of dependencies
    # TODO: there is probably a better way to find this through bitbake 
    # variables?
    export HOST_TOOLCHAIN_NAME="$(echo $CC | cut -f1-1 -d ' ' | awk -F '-' 'sub(FS $NF,x)')"
    echo "[do_compile] This toolchain will be used to build dependencies: $HOST_TOOLCHAIN_NAME" 

    # GOPATH settings and binary directory setup
    export GOPATH=$(pwd)/_dist
    export GOBIN="$GOPATH/bin"
    mkdir -p "$GOBIN"

    # Build dependencies
    oe_runmake deps

    # The build process for LXD expects these variables to be manually set 
    # after 'make deps' and before 'make'
    # TODO: patch 'make deps' to automate this step
    export CGO_CFLAGS="--sysroot=${STAGING_DIR_TARGET} -I$GOPATH/deps/raft/include/ -I$GOPATH/deps/dqlite/include/"
    export CGO_LDFLAGS="--sysroot=${STAGING_DIR_TARGET} -L$GOPATH/deps/raft/.libs -L$GOPATH/deps/dqlite/.libs/"
    export LD_LIBRARY_PATH="$GOPATH/deps/raft/.libs/:$GOPATH/deps/dqlite/.libs/"
    export CGO_LDFLAGS_ALLOW="-Wl,-wrap,pthread_create"

    # Build LXD
    oe_runmake
}

do_install() {
    cd "../${LXD_PKG_NAME}/"

    # Install dependencies' libraries: raft and dqlite
    install -d ${D}${libdir}
    install -m  0644 _dist/deps/raft/.libs/libraft.so* ${D}${libdir}
    install -m  0644 _dist/deps/dqlite/.libs/libdqlite.so* ${D}${libdir}

    # Install built LXD binaries
    install -d ${D}${bindir}
    install -m  0755 _dist/bin/* ${D}${bindir}

    # LXD requires the '/var/lib/lxd' directory during runtime
    install -d ${D}${localstatedir}/lib/lxd

    # We need sub{u,g}ids for root, so that LXD can create the unprivileged containers
    # see https://github.com/lxc/lxd#machine-setup
    install -d ${D}${sysconfdir}
    echo "root:1000000:65536" | tee -a ${D}${sysconfdir}/subuid ${D}${sysconfdir}/subgid
}
