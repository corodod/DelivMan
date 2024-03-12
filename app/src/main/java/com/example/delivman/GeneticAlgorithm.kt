package com.example.delivman
import kotlin.math.sqrt
import kotlin.random.Random
import com.yandex.mapkit.geometry.Point
class GeneticAlgorithm() {
    var matrix: Array<DoubleArray> = arrayOf()
    private var shortestDistance = Double.MAX_VALUE
    private lateinit var bestRouteN: IntArray
    private var populationSize = 0
    private var mutationRate = 0.2
    private var numGenerations = 0
    private var crossoverRate = 0.3

    fun getPath(): IntArray {
        val cntCity = matrix.size
        when {
            cntCity in 3..5 -> {
                populationSize = 30
                mutationRate = 0.2
                numGenerations = 10
                crossoverRate = 0.3
            }
            cntCity in 6..9 -> {
                populationSize = 100
                mutationRate = 0.2
                numGenerations = 600
                crossoverRate = 0.3
            }
            cntCity in 10..20 -> {
                populationSize = 200
                mutationRate = 0.2
                numGenerations = 2000
                crossoverRate = 0.3
            }
            cntCity in 21..35 -> {
                populationSize = 600
                mutationRate = 0.2
                numGenerations = 4000
                crossoverRate = 0.3
            }
            cntCity in 36..60 -> {
                populationSize = 5000
                mutationRate = 0.2
                numGenerations = 2000
                crossoverRate = 0.3
            }
            cntCity > 60 -> {
                populationSize = 10000
                mutationRate = 0.2
                numGenerations = 1000
                crossoverRate = 0.3
            }
        }
        bestRouteN = IntArray(matrix.size)

        var population = generatePopulation()
        repeat(numGenerations) { generation ->
            population = selection(population)
            val newPopulation = ArrayList<List<Int>>()

            repeat(populationSize / 2) {
                val parent1 = getRandomElement(population)
                val parent2 = getRandomElement(population)
                var child1 = parent1
                var child2 = parent2

                if (Math.random() < crossoverRate){
                    child1 = crossover(parent1, parent2)
                    child2 = crossover(parent1, parent2)
                }
                child1 = mutate(child1)
                child2 = mutate(child2)

                newPopulation.add(child1)
                newPopulation.add(child2)
            }
            population = newPopulation
            if (generation % 10 == 0) {
                val bestRoute = getBestRoute(population)
                val distance = calculateDistance(bestRoute)
                if (distance < shortestDistance) {
                    shortestDistance = distance
                }
            }
        }
        val bestRoute = getBestRoute(population)
        shortestDistance = calculateDistance(bestRoute)
        bestRoute.forEachIndexed { index, value -> bestRouteN[index] = value - 1 }

        return bestRouteN
    }

    private fun generatePopulation(): ArrayList<List<Int>> {
        val population = ArrayList<List<Int>>()
        repeat(populationSize) {
            val route = (1..matrix.size).toMutableList()
            val route2 = mutableListOf<Int>()
            route2.add(route[0])
            route.removeAt(0)
            val a = route[route.size - 1]
            route.removeAt(route.size - 1)
            route.shuffle()
            route2.addAll(route)
            route2.add(a)
            population.add(route2 as ArrayList<Int>)
        }
        return population
    }
    fun mutate(route: List<Int>): List<Int> {
        // Создаем изменяемую копию маршрута
        val mutatedRoute = route.toMutableList()
        // Выбираем случайные две точки для мутации из всех точек маршрута, кроме первой и последней
        val index1 = Random.nextInt(1, route.size - 1) // Индекс первой точки для мутации
        var index2 = Random.nextInt(1, route.size - 1) // Индекс второй точки для мутации
        // Убеждаемся, что index1 и index2 различны
        while (index1 == index2) {
            index2 = Random.nextInt(1, route.size - 1)
        }
        // Обмен местами выбранных точек
        mutatedRoute[index1] = mutatedRoute[index2].also { mutatedRoute[index2] = mutatedRoute[index1] }
        return mutatedRoute
    }
    fun selection(population: ArrayList<List<Int>>): ArrayList<List<Int>> {
        val selected = ArrayList<List<Int>>()
        repeat(populationSize) {
            val tournament = getRandomElements(population, 2)
            val winner = getBestRoute(tournament)
            selected.add(winner)
        }
        return selected
    }

    private fun crossover(parent1: List<Int>, parent2: List<Int>): List<Int> {
        val firstFirst = parent1[0]
        val firstSecond = parent2[0]
        val lastFirst = parent1[parent1.size - 1]
        val lastSecond = parent2[parent1.size - 1]
        val parent1Trimmed = parent1.subList(1, parent1.size - 1)
        val parent2Trimmed = parent2.subList(1, parent2.size - 1)

        val size = parent1Trimmed.size
        val start = Random.nextInt(size - 1)
        val end = Random.nextInt(size - start) + start

        val child = parent1Trimmed.subList(start, end + 1).toMutableList()
        parent2Trimmed.forEach { gene ->
            if (!child.contains(gene)) {
                child.add(gene)
            }
        }
        child.add(0, firstFirst)// Добавляем первый и последний элементы обратно
        child.add(lastFirst)
        return child
    }

    fun getRandomElements(population: ArrayList<List<Int>>, count: Int): List<List<Int>> {
        val randomElements = mutableListOf<List<Int>>()
        while (randomElements.size < count) {
            val element = population.random()
            if (element !in randomElements) {
                randomElements.add(element)
            }
        }
        return randomElements
    }
    fun createAdjacencyMatrix(points: List<Point>): Array<DoubleArray> {
        val n = points.size // Получаем количество точек в списке
        val adjacencyMatrix = Array(n) { DoubleArray(n) } // Создаем матрицу смежности заданной размерности
        for (i in 0 until n) {
            for (j in 0 until i) {
                val distance = calculateDistance1(points[i], points[j]) // Вычисляем расстояние между точками i и j
                adjacencyMatrix[i][j] = distance
                adjacencyMatrix[j][i] = distance // Записываем расстояние в соответствующие элементы матрицы смежности
            }
        }
        return adjacencyMatrix // Возвращаем матрицу смежности
    }
    private fun calculateDistance1(point1: Point, point2: Point): Double {
        val v1v = (point1.latitude - point2.latitude) * (point1.latitude - point2.latitude)
        val v2v = (point1.longitude - point2.longitude) * (point1.longitude - point2.longitude)
        return sqrt(v1v + v2v)
    }
    private fun getRandomElement(population: ArrayList<List<Int>>): List<Int> {
        return population.random()
    }
    private fun getBestRoute(population: List<List<Int>>): List<Int> {
        return population.minByOrNull { calculateDistance(it) } ?: emptyList()
    }
    private fun calculateDistance(route: List<Int>): Double {
        var distance = 0.0
        val size = route.size
        for (i in 0 until size-1) {
            val city1 = route[i] - 1
            val city2 = route[(i + 1) % size] - 1
            distance += matrix[city1][city2]
        }
        return distance
    }
}