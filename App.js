import { StyleSheet, Text, View, TouchableOpacity, Dimensions } from 'react-native';
import React, { useEffect, useState } from 'react';
import Torch from 'react-native-torch';
import Slider from '@react-native-community/slider'
const {width, height} = Dimensions.get('screen')

const App = () => {
  const [flashlightIntensity, setFlashlightIntensity] = useState(null);
  const [flashValue, setFlashValue] = useState(0)


  const torchOn = async () => {
    await Torch.switchState(true);

    try {
      const value = await new Promise((resolve, reject) => {
        Torch.getFlashIntensity(
          intensity => resolve(intensity),
          error => reject(error)
        );
      });

      setFlashlightIntensity(value);
      console.log('Success:', value);
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const setFlashlightStrength = async (strength) => {
    try {
      await new Promise((resolve, reject) => {
        Torch.changeFlashlightStrength(
          strength,
          () => {
            resolve();
          },
          error => {
            reject(error);
          }
        );
      });

      console.log('Flashlight strength set successfully');
    } catch (error) {
      console.error('Error setting flashlight strength: ', error);
    }
  };

  useEffect(() => {
    torchOn();
  }, []);

  return (
    <View style={styles.container}>
    <Text style={styles.buttonText}>Set Flashlight Strength</Text>
      <View style={{backgroundColor:'white'}}>
        <Slider
          style={{ width: width*0.6, height:height*0.05 }}
          minimumValue={0.1}
          maximumValue={5}
          value={flashValue}
          minimumTrackTintColor='#FFD700'
          maximumTrackTintColor='gray'
          thumbTintColor={'#FFD700'}
          onValueChange={data => {
            setFlashValue(data)
            setFlashlightStrength(Math.ceil(data))


          }}
        />
        </View>
       
      
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  button: {
    backgroundColor: 'green',
    padding: 10,
    borderRadius: 5,
    marginTop: 10,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
  },
});

export default App;
