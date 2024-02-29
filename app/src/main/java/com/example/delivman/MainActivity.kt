package com.example.delivman
import android.Manifest
import android.annotation.SuppressLint

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Session
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider

class MainActivity : AppCompatActivity(), DrivingSession.DrivingRouteListener {


    private lateinit var mapview:MapView
    var points = mutableListOf<Point>()

    private var mapObjects: MapObjectCollection? = null
    private var drivingeRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("09ef3309-a2b2-4287-834a-36c26ab3a00c")
        MapKitFactory.initialize(this)
        setContentView(R.layout.activity_main)

        val trafficButton:Button = findViewById(R.id.trafficButton)
        val addPointButton:Button = findViewById(R.id.addPointButton)
        mapview = findViewById(R.id.mapview)
        mapview.map.move(
            CameraPosition(Point(51.694917, 39.165636), 11.0f,0.0f,0.0f),
            Animation(Animation.Type.SMOOTH,1f), null)
        val mapKit: MapKit = MapKitFactory.getInstance()

        var pointsFlag = false
        var finishFlag = false
        var wayFlag = false
        val inputListener = object : InputListener {
            override fun onMapTap(map: Map, point: Point) {
                if(!wayFlag){
                    if (points.size==0){
                        points.add(point) // Добавление точки в список points
                        var a = mapview.map.mapObjects.addPlacemark(point, ImageProvider.fromResource(applicationContext, R.drawable.startflag))
                        addPointButton.text = "Задайте точки"
                        pointsFlag = true
                    }else{
                        if(pointsFlag){
                            points.add(point)
                            mapview.map.mapObjects.addPlacemark(point, ImageProvider.fromResource(applicationContext, R.drawable.point))
                        }else{
                            if (finishFlag){
                                //finishFlag = false
                                points.add(point)
                                mapview.map.mapObjects.addPlacemark(point, ImageProvider.fromResource(applicationContext, R.drawable.finishflag))
                                wayFlag = true
                                addPointButton.text = "Построить путь"
                            }
                        }
                    }
                }
            }
            override fun onMapLongTap(map: Map, point: Point) {
                mapview.map.mapObjects.clear()
                points.clear()
                finishFlag = false
                pointsFlag = false
                wayFlag = false
                addPointButton.text = "Задайте старт"
            }
        }
        mapview.map.addInputListener(inputListener)//Регистрация InputListener на карте
        requstLocationPermisiion()
        val probki = mapKit.createTrafficLayer(mapview.mapWindow)
        var probkiison = true
        probki.isTrafficVisible = true
        val locationonmapkit = mapKit.createUserLocationLayer(mapview.mapWindow)
        locationonmapkit.isVisible = true

        trafficButton.setOnClickListener{
            when(probkiison){
                false -> {
                    probkiison = true
                    probki.isTrafficVisible = true
                }
                true -> {
                    probkiison = false
                    probki.isTrafficVisible = false
                }

            }
        }
        addPointButton.setOnClickListener{
            if(points.size!=0 && pointsFlag){
                finishFlag = true
                pointsFlag = false
                addPointButton.text = "Задайте финиш"
            }
            if(wayFlag){
                //todo ЗДЕСЬ ФИНАЛОЧКА
                val algo = GeneticAlgorithm()
                algo.matrix = algo.createAdjacencyMatrix(points)
                val resultIndexes:IntArray = algo.getPath()
                points = reorderPoints(points,resultIndexes)
                //здесь переставить чтобы все было тип топ
                sumbitRequest(points)
            }
        }
        drivingeRouter = DirectionsFactory.getInstance().createDrivingRouter()
        mapObjects = mapview.map.mapObjects.addCollection()
    }

    override fun onStop() {
        mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
    override fun onStart() {
        mapview.onStart()
        MapKitFactory.getInstance().onStart()
        super.onStart()
    }
    private fun requstLocationPermisiion(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),1)
            return
        }
    }

    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        mapObjects!!.addPolyline(p0[0].geometry)
    }
    override fun onDrivingRoutesError(p0: Error) {
        var errorMessage = "Неизвестная ошибка"
        Toast.makeText(this,errorMessage, Toast.LENGTH_SHORT).show()
    }
    private fun sumbitRequest(points: MutableList<Point>){
        val drivingOptions = DrivingOptions()
        val vehicleOptions = VehicleOptions()
        val requestPoints:ArrayList<RequestPoint>  = ArrayList()
        for (point in points){
            requestPoints.add(RequestPoint(point,RequestPointType.WAYPOINT,null))
            Thread.sleep(1000)
        }
        drivingSession = drivingeRouter!!.requestRoutes(requestPoints,drivingOptions,vehicleOptions,this)
    }

    fun reorderPoints(points: MutableList<Point>, order: IntArray): MutableList<Point> {
        // метод для установки
        val reorderedPoints = mutableListOf<Point>()
        for (index in order) {
            reorderedPoints.add(points[index])
        }
        return reorderedPoints
    }
}
