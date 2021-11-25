/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.util.raytrace;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.malisis.core.util.Point;
import net.malisis.core.util.Ray;
import net.malisis.core.util.Vector;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

/**
 * @author Ordinastie
 *
 */
public class Raytrace
{
	/** Source of the ray trace. */
	protected Point src;
	/** Destination of the ray trace. */
	protected Point dest;
	/** Ray describing the ray trace. */
	protected Ray ray;

	/**
	 * Instanciate a new {@link Raytrace}.
	 *
	 * @param ray the ray
	 */
	public Raytrace(Ray ray)
	{
		this.src = ray.origin;
		this.ray = ray;
	}

	/**
	 * Instanciate a new {@link Raytrace}.
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	public Raytrace(Point src, Point dest)
	{
		this(new Ray(src, new Vector(src, dest)));
		this.dest = dest;
	}

	/**
	 * Instanciate a new {@link Raytrace}.
	 *
	 * @param src the src
	 * @param dest the dest
	 */
	public Raytrace(Vector3d src, Vector3d dest)
	{
		this(new Ray(src, dest));
		this.dest = new Point(dest);
	}

	/**
	 * Gets the source of this {@link Raytrace}
	 *
	 * @return the source
	 */
	public Point getSource()
	{
		return src;
	}

	/**
	 * Gets the destination of this {@link Raytrace}.
	 *
	 * @return the destination
	 */
	public Point getDestination()
	{
		return dest;
	}

	/**
	 * Gets the direction vector of the ray.
	 *
	 * @return the direction
	 */
	public Vector direction()
	{
		return ray.direction;
	}

	/**
	 * Gets the length of the ray.
	 *
	 * @return the distance
	 */
	public double distance()
	{
		return ray.direction.length();
	}

	/**
	 * Sets the length of this {@link Raytrace}.
	 *
	 * @param length the new length
	 */
	public void setLength(double length)
	{
		dest = ray.getPointAt(length);
	}

	public Pair<Direction, Point> trace(AxisAlignedBB... aabbs)
	{
		if (ArrayUtils.isEmpty(aabbs))
			return null;

		List<Pair<Direction, Point>> points = new ArrayList<>();
		double maxDist = dest != null ? Point.distanceSquared(src, dest) : Double.MAX_VALUE;
		for (AxisAlignedBB aabb : aabbs)
		{
			if (aabb == null)
				continue;

			for (Pair<Direction, Point> pair : ray.intersect(aabb))
			{
				if (Point.distanceSquared(src, pair.getRight()) < maxDist)
					points.add(pair);
			}
		}

		if (points.size() == 0)
			return null;

		return getClosest(points);
	}

	/**
	 * Gets the closest {@link Point} of the origin.
	 *
	 * @param points the points
	 * @return the closest point
	 */
	private Pair<Direction, Point> getClosest(List<Pair<Direction, Point>> points)
	{
		double distance = Double.MAX_VALUE;
		Pair<Direction, Point> ret = null;
		for (Pair<Direction, Point> pair : points)
		{
			double d = Point.distanceSquared(src, pair.getRight());
			if (distance > d)
			{
				distance = d;
				ret = pair;
			}
		}

		return ret;
	}

	/**
	 * Gets the closest {@link RayTraceResult} to the source.
	 *
	 * @param src the src
	 * @param result1 the mop1
	 * @param result2 the mop2
	 * @return the closest
	 */
	public static RayTraceResult getClosestHit(RayTraceResult.Type hitType, Point src, RayTraceResult result1, RayTraceResult result2)
	{
		if (result1 == null)
			return result2;
		if (result2 == null)
			return result1;

		if (result1.getType() == RayTraceResult.Type.MISS && result2.getType() == hitType)
			return result2;
		if (result1.getType() == hitType && result2.getType() == RayTraceResult.Type.MISS)
			return result1;

		if (Point.distanceSquared(src, new Point(result1.getLocation())) > Point.distanceSquared(src, new Point(result2.getLocation())))
			return result2;
		return result1;
	}
}
