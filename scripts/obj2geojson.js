const fs = require('fs');
const path = require('path');

// Usage: node scripts/obj2geojson.js <input.obj>
// The source .obj is not tracked in this repository, so it must be passed in explicitly.
const inputArg = process.argv[2];
if (!inputArg) {
    console.error('Usage: node scripts/obj2geojson.js <input.obj>');
    process.exit(1);
}
if (!fs.existsSync(inputArg)) {
    console.error('Input file not found:', inputArg);
    process.exit(1);
}

const content = fs.readFileSync(inputArg, 'utf8');
const lines = content.split('\n');

const positions = [];
const uvs = [];
const triangles = [];
const uvIndices = [];

for (const line of lines) {
  const t = line.trim();
  if (t.startsWith('v ')) {
    const p = t.split(/\s+/).slice(1).map(Number);
    positions.push(p);
  } else if (t.startsWith('vt ')) {
    const p = t.split(/\s+/).slice(1).map(Number);
    uvs.push([p[0], 1 - p[1]]);
  } else if (t.startsWith('f ')) {
    const parts = t.split(/\s+/).slice(1);
    for (let i = 1; i < parts.length - 1; i++) {
      const [v1, t1] = parts[0].split('/').map(Number);
      const [v2, t2] = parts[i].split('/').map(Number);
      const [v3, t3] = parts[i + 1].split('/').map(Number);
      triangles.push([v1 - 1, v2 - 1, v3 - 1]);
      uvIndices.push([t1 - 1, t2 - 1, t3 - 1]);
    }
  }
}

// Compute per-vertex normals
const normals = positions.map(() => [0, 0, 0]);
for (const tri of triangles) {
  const p0 = positions[tri[0]];
  const p1 = positions[tri[1]];
  const p2 = positions[tri[2]];
  const vx = p1[0] - p0[0], vy = p1[1] - p0[1], vz = p1[2] - p0[2];
  const wx = p2[0] - p0[0], wy = p2[1] - p0[1], wz = p2[2] - p0[2];
  const nx = vy * wz - vz * wy;
  const ny = vz * wx - vx * wz;
  const nz = vx * wy - vy * wx;
  const len = Math.sqrt(nx * nx + ny * ny + nz * nz) || 1;
  const inv = 1 / len;
  for (const i of tri) {
    normals[i][0] += nx * inv;
    normals[i][1] += ny * inv;
    normals[i][2] += nz * inv;
  }
}
for (const n of normals) {
  const len = Math.sqrt(n[0] * n[0] + n[1] * n[1] + n[2] * n[2]) || 1;
  n[0] /= len; n[1] /= len; n[2] /= len;
}

// Scale to Minecraft coordinates
const scale = 2;
const scaledPos = positions.map(p => [p[0] * scale, p[1] * scale, p[2] * scale]);

// Clamp UVs to 0-1
const clampedUvs = uvs.map(uv => [
  Math.max(0, Math.min(1, uv[0])),
  Math.max(0, Math.min(1, uv[1]))
]);

const flatPos = [];
for (const p of scaledPos) flatPos.push(...p);
const flatUvs = [];
for (const uv of clampedUvs) flatUvs.push(...uv);
const flatNorms = [];
for (const n of normals) flatNorms.push(...n);

const tri = [];
for (const t of triangles) tri.push(...t);

const geoJson = {
  format_version: "1.12.0",
  "minecraft:geometry": [
    {
      description: {
        identifier: "geometry.model.boosted_gear",
        texture_width: 64,
        texture_height: 64,
        visible_bounds_width: 3,
        visible_bounds_height: 4,
        visible_bounds_offset: [0, 1, 0]
      },
      bones: [
        {
          name: "root",
          pivot: [0, 0, 0],
          poly_mesh: {
            normalized_uvs: true,
            positions: flatPos,
            normals: flatNorms,
            uvs: flatUvs,
            polys: {
              triangle_list: tri
            }
          }
        }
      ]
    }
  ]
};

// Resolved relative to the repository root so the script works on any machine.
const outputPath = path.resolve(
    __dirname, '..', 'src', 'main', 'resources', 'assets', 'raymovies', 'geo', 'item', 'boosted_gear.geo.json');
fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, JSON.stringify(geoJson, null, 2));
const stats = fs.statSync(outputPath);
console.log('Done! Written to', outputPath);
console.log(`Vertices: ${positions.length}, Triangles: ${triangles.length}, File: ${(stats.size / 1024).toFixed(1)}KB`);
