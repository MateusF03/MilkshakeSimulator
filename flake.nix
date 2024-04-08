{
  description = "Java Template";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-compat.url = "github:edolstra/flake-compat";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-compat, flake-utils }:
    flake-utils.lib.eachDefaultSystem
      (system:
        let
          # Set package name
          pname = "MilkshakeSimulator";
          # Get set of packages
          pkgs = import nixpkgs {
            inherit system;
          };
          # Add dev shell dependencies
          devShellInputs = with pkgs; [
            # ...
          ];
          # Add compile-time dependencies
          nativeBuildInputs = with pkgs; [
            # ...
          ];
          # Add run-time dependencies
          buildInputs = with pkgs; [
            jre17_minimal
            corefonts
            vistafonts
            fontconfig
            imagemagick
          ];
          # Define Gradle build
          buildGradle = pkgs.callPackage ./nix/gradle-env.nix { gradleBuildJdk = pkgs.jdk17; };
          # Common environment variables
          LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath buildInputs}";
        in {
          packages = rec {
            default = MilkshakeSimulator;
            MilkshakeSimulator = buildGradle {
              inherit nativeBuildInputs buildInputs LD_LIBRARY_PATH;

              envSpec = ./nix/gradle-env.json;
              
              src = with pkgs; lib.cleanSourceWith {
                filter = lib.cleanSourceFilter;
                src = lib.cleanSourceWith {
                  filter = path: type: let baseName = baseNameOf path; in !(
                    (type == "directory" && (
                      baseName == "build" ||
                      baseName == ".idea" ||
                      baseName == ".gradle"
                    )) ||
                    (lib.hasSuffix ".iml" baseName)
                  );
                  src = ./.;
                };
              };

              gradleFlags = [ "shadowJar" ];

              installPhase = ''
                mkdir -p $out
                cp -r ./build/libs/${pname}*.jar $out/
              '';

              passthru = {
                plugin = "${MilkshakeSimulator}/share/plugin.jar";
              };
            };
            dockerImage = pkgs.dockerTools.buildLayeredImage {
              name = "MilkshakeSimulator";
              tag = "head";
              contents = [ default ];
            };
          };

          devShells.default = pkgs.mkShell {
            buildInputs = self.packages.${system}.default.buildInputs;
            nativeBuildInputs = devShellInputs ++ self.packages.${system}.default.nativeBuildInputs;
          };
        }
      );
}
