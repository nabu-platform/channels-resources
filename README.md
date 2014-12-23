# Resource Channel Provider

This package contains channel providers that use the resource system (a low level VFS) to move data around.

It also contains an implementation for a `ChannelRewriter` that uses the same evaluation engine as [glue](https://github.com/nablex/glue).

## Connection Handling

The directory poller will scan a directory for certain files and start a new instance of the file provider to pick up each file. Internally it will continue to use the resources instead of resolving them again. This maintains whatever connection or other state was set up to do the original directory scan.